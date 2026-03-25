// package org.netflix.Services;

package org.netflix.Services;

import org.netflix.DAO.*;
import org.netflix.Models.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class PlaybackService {

    private final WatchHistoryDAO watchHistoryDAO;
    private final WatchProgressDAO watchProgressDAO;
    private final MovieDAO movieDAO;
    private final EpisodeDAO episodeDAO;
    private final SeasonDAO seasonDAO;
    private final SerieDAO serieDAO;
    private final SeriesService seriesService;

    public PlaybackService() {
        this.watchHistoryDAO = new WatchHistoryDAO();
        this.watchProgressDAO = new WatchProgressDAO();
        this.movieDAO = new MovieDAO();
        this.episodeDAO = new EpisodeDAO();
        this.seasonDAO = new SeasonDAO();
        this.serieDAO = new SerieDAO();
        this.seriesService = new SeriesService();
    }

    // ==================== MÉTHODES POUR LES FILMS ====================

    /**
     * Démarre la lecture d'un film
     * @param userId ID de l'utilisateur
     * @param movieId ID du film
     * @return Informations de lecture (URL vidéo, position de reprise)
     */
    public PlaybackInfo startMovie(int userId, int movieId) {
        Optional<Movie> movieOpt = movieDAO.findById(movieId);

        if (movieOpt.isEmpty()) {
            throw new IllegalArgumentException("Film non trouvé");
        }

        Movie movie = movieOpt.get();

        // Vérifier s'il y a une progression sauvegardée
        Optional<WatchHistory> historyOpt = watchHistoryDAO.findByUserAndMovie(userId, movieId);

        double resumePosition = 0.0;
        boolean isResume = false;

        if (historyOpt.isPresent()) {
            WatchHistory history = historyOpt.get();
            // Si l'utilisateur a regardé plus de 90% du film, on recommence du début
            double duration = movie.getDurationMinutes() * 60.0; // Convertir en secondes
            if (history.getStoppedAtTime() < duration * 0.9) {
                resumePosition = history.getStoppedAtTime();
                isResume = true;
            }
        }

        return new PlaybackInfo(
                movie.getVideoUrl(),
                movie.getTitle(),
                movie.getDurationMinutes() * 60.0, // durée en secondes
                resumePosition,
                isResume,
                "MOVIE",
                movieId
        );
    }

    // ==================== MÉTHODES POUR LES ÉPISODES ====================

    /**
     * Démarre la lecture d'un épisode
     * @param userId ID de l'utilisateur
     * @param episodeId ID de l'épisode
     * @return Informations de lecture (URL vidéo, position de reprise)
     */
    public PlaybackInfo startEpisode(int userId, int episodeId) {
        Optional<Episode> episodeOpt = episodeDAO.findById(episodeId);

        if (episodeOpt.isEmpty()) {
            throw new IllegalArgumentException("Épisode non trouvé");
        }

        Episode episode = episodeOpt.get();

        // Vérifier s'il y a une progression sauvegardée
        Optional<WatchProgress> progressOpt = watchProgressDAO.findByUserAndEpisode(userId, episodeId);

        double resumePosition = 0.0;
        boolean isResume = false;

        if (progressOpt.isPresent()) {
            WatchProgress progress = progressOpt.get();
            if (!progress.isCompleted()) {
                resumePosition = progress.getStoppedAtTime();
                isResume = true;
            }
        }

        // Récupérer la durée de l'épisode (à définir dans la base)
        double duration = getEpisodeDuration(episode);

        return new PlaybackInfo(
                episode.getFilePath(),
                episode.getTitle(),
                duration,
                resumePosition,
                isResume,
                "EPISODE",
                episodeId
        );
    }

    // ==================== ENREGISTREMENT DE LA PROGRESSION ====================

    /**
     * Enregistre la progression de lecture d'un film
     * @param userId ID de l'utilisateur
     * @param movieId ID du film
     * @param currentPosition Position actuelle en secondes
     * @param duration Durée totale en secondes
     */
    public void saveMovieProgress(int userId, int movieId, double currentPosition, double duration) {
        boolean isCompleted = currentPosition >= duration * 0.95;

        // Mettre à jour ou créer l'historique
        Optional<WatchHistory> existingOpt = watchHistoryDAO.findByUserAndMovie(userId, movieId);

        if (existingOpt.isPresent()) {
            WatchHistory history = existingOpt.get();
            history.setStoppedAtTime(currentPosition);
            history.setLastWatched(Timestamp.valueOf(LocalDateTime.now()));
            watchHistoryDAO.update(history);
        } else {
            WatchHistory history = new WatchHistory(
                    userId,
                    movieId,
                    null,
                    currentPosition,
                    Timestamp.valueOf(LocalDateTime.now())
            );
            watchHistoryDAO.insert(history);
        }

        // Enregistrer la progression pour les statistiques
        recordPlaybackSession(userId, movieId, null, currentPosition, duration, isCompleted);
    }

    /**
     * Enregistre la progression de lecture d'un épisode
     * @param userId ID de l'utilisateur
     * @param episodeId ID de l'épisode
     * @param currentPosition Position actuelle en secondes
     * @param duration Durée totale en secondes
     */
    public void saveEpisodeProgress(int userId, int episodeId, double currentPosition, double duration) {
        boolean isCompleted = currentPosition >= duration * 0.95;

        // Mettre à jour ou créer la progression
        Optional<WatchProgress> existingOpt = watchProgressDAO.findByUserAndEpisode(userId, episodeId);

        if (existingOpt.isPresent()) {
            WatchProgress progress = existingOpt.get();
            progress.setStoppedAtTime(currentPosition);
            progress.setCompleted(isCompleted);
            progress.setLastUpdated(Timestamp.valueOf(LocalDateTime.now()));
            watchProgressDAO.update(progress);
        } else {
            WatchProgress progress = new WatchProgress(
                    userId,
                    episodeId,
                    currentPosition,
                    isCompleted,
                    Timestamp.valueOf(LocalDateTime.now())
            );
            watchProgressDAO.insert(progress);
        }

        // Enregistrer dans l'historique général
        recordPlaybackSession(userId, null, episodeId, currentPosition, duration, isCompleted);

        // Si l'épisode est terminé, proposer l'épisode suivant (binge-watching)
        if (isCompleted) {
            handleBingeWatching(userId, episodeId);
        }
    }

    /**
     * Enregistre une session de lecture dans l'historique
     */
    private void recordPlaybackSession(int userId, Integer movieId, Integer episodeId,
                                       double position, double duration, boolean completed) {
        // Cette méthode peut être utilisée pour des statistiques avancées
        // Par exemple, enregistrer chaque session dans une table analytics
        System.out.println("Session enregistrée - User: " + userId +
                ", Position: " + position + "/" + duration +
                ", Completed: " + completed);
    }

    // ==================== BINGE-WATCHING ====================

    /**
     * Gère la logique de binge-watching : propose l'épisode suivant
     * @param userId ID de l'utilisateur
     * @param completedEpisodeId ID de l'épisode terminé
     */
    private void handleBingeWatching(int userId, int completedEpisodeId) {
        Optional<Episode> episodeOpt = episodeDAO.findById(completedEpisodeId);

        if (episodeOpt.isEmpty()) {
            return;
        }

        Episode currentEpisode = episodeOpt.get();
        Optional<Season> seasonOpt = seasonDAO.findById(currentEpisode.getSeasonId());

        if (seasonOpt.isEmpty()) {
            return;
        }

        Season season = seasonOpt.get();

        // Chercher l'épisode suivant
        Optional<Episode> nextEpisodeOpt = seriesService.getNextEpisode(
                season.getIsSerie(),
                season.getSeasonNumber(),
                currentEpisode.getEpisodeNumber()
        );

        if (nextEpisodeOpt.isPresent()) {
            Episode nextEpisode = nextEpisodeOpt.get();
            // Ici on pourrait déclencher un événement pour afficher un compte à rebours
            // Le contrôleur UI se chargera d'afficher la proposition
            System.out.println("Binge-watching: Épisode suivant disponible - " + nextEpisode.getTitle());
        }
    }

    /**
     * Vérifie si un épisode suivant est disponible pour le binge-watching
     * @param userId ID de l'utilisateur
     * @param currentEpisodeId ID de l'épisode actuel
     * @return Optional contenant l'épisode suivant
     */
    public Optional<Episode> getNextEpisodeForBingeWatching(int userId, int currentEpisodeId) {
        Optional<Episode> currentOpt = episodeDAO.findById(currentEpisodeId);

        if (currentOpt.isEmpty()) {
            return Optional.empty();
        }

        Episode current = currentOpt.get();
        Optional<Season> seasonOpt = seasonDAO.findById(current.getSeasonId());

        if (seasonOpt.isEmpty()) {
            return Optional.empty();
        }

        Season season = seasonOpt.get();

        return seriesService.getNextEpisode(
                season.getIsSerie(),
                season.getSeasonNumber(),
                current.getEpisodeNumber()
        );
    }

    // ==================== REPRISE INTELLIGENTE ====================

    /**
     * Récupère le point de reprise pour un film
     * @param userId ID de l'utilisateur
     * @param movieId ID du film
     * @return Point de reprise en secondes
     */
    public double getMovieResumePoint(int userId, int movieId) {
        Optional<WatchHistory> historyOpt = watchHistoryDAO.findByUserAndMovie(userId, movieId);

        if (historyOpt.isEmpty()) {
            return 0.0;
        }

        WatchHistory history = historyOpt.get();

        // Récupérer la durée du film
        Optional<Movie> movieOpt = movieDAO.findById(movieId);
        if (movieOpt.isPresent()) {
            double duration = movieOpt.get().getDurationMinutes() * 60.0;
            // Si l'utilisateur a regardé plus de 90%, on recommence
            if (history.getStoppedAtTime() >= duration * 0.9) {
                return 0.0;
            }
        }

        return history.getStoppedAtTime();
    }

    /**
     * Récupère le point de reprise pour un épisode
     * @param userId ID de l'utilisateur
     * @param episodeId ID de l'épisode
     * @return Point de reprise en secondes
     */
    public double getEpisodeResumePoint(int userId, int episodeId) {
        Optional<WatchProgress> progressOpt = watchProgressDAO.findByUserAndEpisode(userId, episodeId);

        if (progressOpt.isEmpty()) {
            return 0.0;
        }

        WatchProgress progress = progressOpt.get();

        if (progress.isCompleted()) {
            return 0.0;
        }

        return progress.getStoppedAtTime();
    }

    // ==================== HISTORIQUE DE LECTURE ====================

    /**
     * Récupère l'historique des vidéos visionnées par un utilisateur
     * @param userId ID de l'utilisateur
     * @param limit Nombre maximum d'éléments
     * @return Liste des éléments d'historique
     */
    public List<HistoryItem> getWatchHistory(int userId, int limit) {
        List<HistoryItem> historyItems = new ArrayList<>();

        // Récupérer l'historique des films
        List<WatchHistory> movieHistory = watchHistoryDAO.findByUser(userId);

        for (WatchHistory history : movieHistory) {
            if (history.getMovieId() != null) {
                Optional<Movie> movieOpt = movieDAO.findById(history.getMovieId());
                if (movieOpt.isPresent()) {
                    Movie movie = movieOpt.get();
                    historyItems.add(new HistoryItem(
                            movie.getIdMedia(),
                            movie.getTitle(),
                            movie.getCoverImageUrl(),
                            "MOVIE",
                            history.getLastWatched(),
                            history.getStoppedAtTime() / (movie.getDurationMinutes() * 60.0) // progression %
                    ));
                }
            }
        }

        // Récupérer l'historique des épisodes
        List<WatchProgress> episodeProgress = watchProgressDAO.findByUser(userId);

        for (WatchProgress progress : episodeProgress) {
            Optional<Episode> episodeOpt = episodeDAO.findById(progress.getEpisodeId());
            if (episodeOpt.isPresent()) {
                Episode episode = episodeOpt.get();
                Optional<Season> seasonOpt = seasonDAO.findById(episode.getSeasonId());
                if (seasonOpt.isPresent()) {
                    Season season = seasonOpt.get();
                    Optional<Serie> serieOpt = serieDAO.findById(season.getIsSerie());
                    if (serieOpt.isPresent()) {
                        Serie serie = serieOpt.get();
                        String title = serie.getTitle() + " - S" + season.getSeasonNumber() +
                                " E" + episode.getEpisodeNumber() + ": " + episode.getTitle();
                        double duration = getEpisodeDuration(episode);
                        double progressPercent = duration > 0 ? progress.getStoppedAtTime() / duration : 0;

                        historyItems.add(new HistoryItem(
                                episode.getId(),
                                title,
                                episode.getThumbnailPath(),
                                "EPISODE",
                                progress.getLastUpdated(),
                                progressPercent
                        ));
                    }
                }
            }
        }

        // Trier par date (plus récent d'abord)
        historyItems.sort((h1, h2) -> h2.getLastWatched().compareTo(h1.getLastWatched()));

        // Limiter le nombre d'éléments
        if (limit > 0 && historyItems.size() > limit) {
            historyItems = historyItems.subList(0, limit);
        }

        return historyItems;
    }

    /**
     * Vérifie si un utilisateur a déjà regardé un film
     * @param userId ID de l'utilisateur
     * @param movieId ID du film
     * @return true si déjà regardé
     */
    public boolean hasWatchedMovie(int userId, int movieId) {
        Optional<WatchHistory> historyOpt = watchHistoryDAO.findByUserAndMovie(userId, movieId);
        return historyOpt.isPresent();
    }

    /**
     * Vérifie si un utilisateur a déjà regardé un épisode
     * @param userId ID de l'utilisateur
     * @param episodeId ID de l'épisode
     * @return true si déjà regardé
     */
    public boolean hasWatchedEpisode(int userId, int episodeId) {
        Optional<WatchProgress> progressOpt = watchProgressDAO.findByUserAndEpisode(userId, episodeId);
        return progressOpt.isPresent() && progressOpt.get().isCompleted();
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Récupère la durée d'un épisode (à adapter selon votre base de données)
     * @param episode L'épisode
     * @return Durée en secondes
     */
    private double getEpisodeDuration(Episode episode) {
        // Dans votre modèle Episode, vous n'avez pas de durée
        // Vous pouvez soit :
        // 1. Ajouter un champ duration dans Episode
        // 2. Récupérer la durée depuis un autre endroit
        // 3. Utiliser une valeur par défaut

        // Pour l'instant, on retourne une valeur par défaut de 45 minutes
        return 45 * 60.0;
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Classe contenant les informations de lecture
     */
    public static class PlaybackInfo {
        private final String videoUrl;
        private final String title;
        private final double duration;
        private final double resumePosition;
        private final boolean isResume;
        private final String type;
        private final int contentId;

        public PlaybackInfo(String videoUrl, String title, double duration,
                            double resumePosition, boolean isResume, String type, int contentId) {
            this.videoUrl = videoUrl;
            this.title = title;
            this.duration = duration;
            this.resumePosition = resumePosition;
            this.isResume = isResume;
            this.type = type;
            this.contentId = contentId;
        }

        public String getVideoUrl() { return videoUrl; }
        public String getTitle() { return title; }
        public double getDuration() { return duration; }
        public double getResumePosition() { return resumePosition; }
        public boolean isResume() { return isResume; }
        public String getType() { return type; }
        public int getContentId() { return contentId; }
    }

    /**
     * Classe pour l'historique de lecture
     */
    public static class HistoryItem {
        private final int contentId;
        private final String title;
        private final String thumbnailUrl;
        private final String type;
        private final Timestamp lastWatched;
        private final double progress;

        public HistoryItem(int contentId, String title, String thumbnailUrl,
                           String type, Timestamp lastWatched, double progress) {
            this.contentId = contentId;
            this.title = title;
            this.thumbnailUrl = thumbnailUrl;
            this.type = type;
            this.lastWatched = lastWatched;
            this.progress = progress;
        }

        public int getContentId() { return contentId; }
        public String getTitle() { return title; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public String getType() { return type; }
        public Timestamp getLastWatched() { return lastWatched; }
        public double getProgress() { return progress; }
        public String getProgressPercentage() { return String.format("%.0f%%", progress * 100); }
    }
}