// package org.netflix.Services;

package org.netflix.Services;

import org.netflix.DAO.*;
import org.netflix.Models.*;

import java.util.*;
import java.util.stream.Collectors;

public class SeriesService {

    private final SerieDAO serieDAO;
    private final SeasonDAO seasonDAO;
    private final EpisodeDAO episodeDAO;
    private final WatchProgressDAO watchProgressDAO;
    private final MediaGenreDAO mediaGenreDAO;
    private final RatingDAO ratingDAO;

    public SeriesService() {
        this.serieDAO = new SerieDAO();
        this.seasonDAO = new SeasonDAO();
        this.episodeDAO = new EpisodeDAO();
        this.watchProgressDAO = new WatchProgressDAO();
        this.mediaGenreDAO = new MediaGenreDAO();
        this.ratingDAO = new RatingDAO();
    }

    // ==================== MÉTHODES POUR LES SÉRIES ====================

    /**
     * Récupère une série complète avec toutes ses saisons et épisodes
     * @param serieId ID de la série
     * @return Optional contenant la série complète
     */
    public Optional<Serie> getSerieComplete(int serieId) {
        Optional<Serie> serieOpt = serieDAO.findById(serieId);

        if (serieOpt.isEmpty()) {
            return Optional.empty();
        }

        Serie serie = serieOpt.get();

        // Charger toutes les saisons de la série
        List<Season> seasons = seasonDAO.findBySerieId(serieId);

        // Pour chaque saison, charger ses épisodes
        for (Season season : seasons) {
            List<Episode> episodes = episodeDAO.findBySeasonId(season.getIdSeason());
            season.setEpisodes(episodes);
        }

        serie.setSeasons(seasons);
        serie.setNbrSaison(seasons.size());

        // Charger les genres de la série
        List<Genre> genres = mediaGenreDAO.findGenresByMedia(serieId, "SERIE");
        serie.getGenres().addAll(genres);

        // Charger la note moyenne
        double avgRating = ratingDAO.calculateAverageRating(serieId);
        serie.setAverageRating(avgRating);

        return Optional.of(serie);
    }

    /**
     * Récupère toutes les séries avec leurs informations de base
     * @return Liste des séries
     */
    public List<Serie> getAllSeries() {
        List<Serie> series = serieDAO.findAll();

        // Enrichir chaque série avec son nombre de saisons et sa note moyenne
        for (Serie serie : series) {
            int seasonCount = seasonDAO.countBySerieId(serie.getIdMedia());
            serie.setNbrSaison(seasonCount);

            double avgRating = ratingDAO.calculateAverageRating(serie.getIdMedia());
            serie.setAverageRating(avgRating);
        }

        return series;
    }

    /**
     * Récupère les séries par genre
     * @param genreName Nom du genre
     * @return Liste des séries du genre
     */
    public List<Serie> getSeriesByGenre(String genreName) {
        Optional<Genre> genreOpt = new GenreDAO().findByName(genreName);

        if (genreOpt.isEmpty()) {
            return new ArrayList<>();
        }

        return serieDAO.findByGenre(genreOpt.get().getId());
    }

    /**
     * Recherche des séries par titre, description ou réalisateur
     * @param query Mot-clé de recherche
     * @return Liste des séries correspondantes
     */
    public List<Serie> searchSeries(String query) {
        return serieDAO.search(query);
    }

    // ==================== MÉTHODES POUR LES SAISONS ====================

    /**
     * Récupère toutes les saisons d'une série
     * @param serieId ID de la série
     * @return Liste des saisons
     */
    public List<Season> getSeasonsBySerie(int serieId) {
        List<Season> seasons = seasonDAO.findBySerieId(serieId);

        // Pour chaque saison, compter le nombre d'épisodes
        for (Season season : seasons) {
            int episodeCount = episodeDAO.countBySeasonId(season.getIdSeason());
            season.setEpisodeCount(episodeCount);
        }

        return seasons;
    }

    /**
     * Récupère une saison spécifique avec ses épisodes
     * @param serieId ID de la série
     * @param seasonNumber Numéro de la saison
     * @return Optional contenant la saison avec ses épisodes
     */
    public Optional<Season> getSeasonByNumber(int serieId, int seasonNumber) {
        Optional<Season> seasonOpt = seasonDAO.findBySerieIdAndNumber(serieId, seasonNumber);

        if (seasonOpt.isPresent()) {
            Season season = seasonOpt.get();
            List<Episode> episodes = episodeDAO.findBySeasonId(season.getIdSeason());
            season.setEpisodes(episodes);
            return Optional.of(season);
        }

        return Optional.empty();
    }

    // ==================== MÉTHODES POUR LES ÉPISODES ====================

    /**
     * Récupère tous les épisodes d'une saison
     * @param seasonId ID de la saison
     * @return Liste des épisodes
     */
    public List<Episode> getEpisodesBySeason(int seasonId) {
        return episodeDAO.findBySeasonId(seasonId);
    }

    /**
     * Récupère un épisode spécifique
     * @param episodeId ID de l'épisode
     * @return Optional contenant l'épisode
     */
    public Optional<Episode> getEpisodeById(int episodeId) {
        return episodeDAO.findById(episodeId);
    }

    /**
     * Récupère l'épisode suivant dans la série
     * @param serieId ID de la série
     * @param currentSeasonNum Numéro de la saison actuelle
     * @param currentEpisodeNum Numéro de l'épisode actuel
     * @return Optional contenant l'épisode suivant, ou vide si c'est le dernier
     */
    public Optional<Episode> getNextEpisode(int serieId, int currentSeasonNum, int currentEpisodeNum) {
        // Vérifier s'il y a un épisode suivant dans la même saison
        Optional<Season> currentSeasonOpt = seasonDAO.findBySerieIdAndNumber(serieId, currentSeasonNum);

        if (currentSeasonOpt.isPresent()) {
            Season currentSeason = currentSeasonOpt.get();
            List<Episode> episodes = episodeDAO.findBySeasonId(currentSeason.getIdSeason());

            // Chercher l'épisode suivant dans la même saison
            Optional<Episode> nextEpisode = episodes.stream()
                    .filter(e -> e.getEpisodeNumber() == currentEpisodeNum + 1)
                    .findFirst();

            if (nextEpisode.isPresent()) {
                return nextEpisode;
            }
        }

        // Sinon, passer à la saison suivante
        Optional<Season> nextSeasonOpt = seasonDAO.findBySerieIdAndNumber(serieId, currentSeasonNum + 1);

        if (nextSeasonOpt.isPresent()) {
            List<Episode> episodes = episodeDAO.findBySeasonId(nextSeasonOpt.get().getIdSeason());
            if (!episodes.isEmpty()) {
                // Retourner le premier épisode de la saison suivante
                return episodes.stream()
                        .min(Comparator.comparingInt(Episode::getEpisodeNumber));
            }
        }

        return Optional.empty();
    }

    /**
     * Récupère l'épisode précédent dans la série
     * @param serieId ID de la série
     * @param currentSeasonNum Numéro de la saison actuelle
     * @param currentEpisodeNum Numéro de l'épisode actuel
     * @return Optional contenant l'épisode précédent, ou vide si c'est le premier
     */
    public Optional<Episode> getPreviousEpisode(int serieId, int currentSeasonNum, int currentEpisodeNum) {
        // Vérifier s'il y a un épisode précédent dans la même saison
        Optional<Season> currentSeasonOpt = seasonDAO.findBySerieIdAndNumber(serieId, currentSeasonNum);

        if (currentSeasonOpt.isPresent()) {
            Season currentSeason = currentSeasonOpt.get();
            List<Episode> episodes = episodeDAO.findBySeasonId(currentSeason.getIdSeason());

            // Chercher l'épisode précédent dans la même saison
            Optional<Episode> prevEpisode = episodes.stream()
                    .filter(e -> e.getEpisodeNumber() == currentEpisodeNum - 1)
                    .findFirst();

            if (prevEpisode.isPresent()) {
                return prevEpisode;
            }
        }

        // Sinon, passer à la saison précédente
        if (currentSeasonNum > 1) {
            Optional<Season> prevSeasonOpt = seasonDAO.findBySerieIdAndNumber(serieId, currentSeasonNum - 1);

            if (prevSeasonOpt.isPresent()) {
                List<Episode> episodes = episodeDAO.findBySeasonId(prevSeasonOpt.get().getIdSeason());
                if (!episodes.isEmpty()) {
                    // Retourner le dernier épisode de la saison précédente
                    return episodes.stream()
                            .max(Comparator.comparingInt(Episode::getEpisodeNumber));
                }
            }
        }

        return Optional.empty();
    }

    // ==================== MÉTHODES DE REPRISE INTELLIGENTE ====================

    /**
     * Récupère le point de reprise pour une série (premier épisode non vu ou en cours)
     * @param userId ID de l'utilisateur
     * @param serieId ID de la série
     * @return Résultat contenant l'épisode à reprendre et sa position
     */
    public ResumeResult getResumePoint(int userId, int serieId) {
        // Récupérer toutes les saisons de la série dans l'ordre
        List<Season> seasons = seasonDAO.findBySerieId(serieId);

        // Trier les saisons par numéro
        seasons.sort(Comparator.comparingInt(Season::getSeasonNumber));

        for (Season season : seasons) {
            List<Episode> episodes = episodeDAO.findBySeasonId(season.getIdSeason());
            episodes.sort(Comparator.comparingInt(Episode::getEpisodeNumber));

            for (Episode episode : episodes) {
                // Vérifier l'état de progression de l'épisode
                Optional<WatchProgress> progressOpt = watchProgressDAO.findByUserAndEpisode(userId, episode.getId());

                if (progressOpt.isEmpty()) {
                    // Épisode jamais commencé → point de reprise ici
                    return new ResumeResult(episode, 0.0, false);
                }

                WatchProgress progress = progressOpt.get();

                if (!progress.isCompleted()) {
                    // Épisode en cours → reprendre à la position sauvegardée
                    return new ResumeResult(episode, progress.getStoppedAtTime(), true);
                }
                // Si l'épisode est terminé, continuer à chercher le suivant
            }
        }

        // Tous les épisodes sont terminés → retourner le dernier épisode (revoir)
        Season lastSeason = seasons.get(seasons.size() - 1);
        List<Episode> lastEpisodes = episodeDAO.findBySeasonId(lastSeason.getIdSeason());
        Episode lastEpisode = lastEpisodes.stream()
                .max(Comparator.comparingInt(Episode::getEpisodeNumber))
                .orElse(null);

        if (lastEpisode != null) {
            return new ResumeResult(lastEpisode, 0.0, false);
        }

        return null;
    }

    /**
     * Récupère le statut de tous les épisodes pour un utilisateur
     * @param userId ID de l'utilisateur
     * @param serieId ID de la série
     * @return Map contenant pour chaque épisode son statut (WATCHED, IN_PROGRESS, NOT_STARTED)
     */
    public Map<Integer, EpisodeStatus> getEpisodesStatus(int userId, int serieId) {
        Map<Integer, EpisodeStatus> statusMap = new HashMap<>();

        // Récupérer tous les épisodes de la série
        List<Episode> allEpisodes = episodeDAO.findBySerieId(serieId);

        for (Episode episode : allEpisodes) {
            Optional<WatchProgress> progressOpt = watchProgressDAO.findByUserAndEpisode(userId, episode.getId());

            if (progressOpt.isEmpty()) {
                statusMap.put(episode.getId(), EpisodeStatus.NOT_STARTED);
            } else {
                WatchProgress progress = progressOpt.get();
                if (progress.isCompleted()) {
                    statusMap.put(episode.getId(), EpisodeStatus.WATCHED);
                } else {
                    statusMap.put(episode.getId(), EpisodeStatus.IN_PROGRESS);
                }
            }
        }

        return statusMap;
    }

    /**
     * Vérifie si un utilisateur a terminé une saison complète
     * @param userId ID de l'utilisateur
     * @param seasonId ID de la saison
     * @return true si tous les épisodes sont terminés, false sinon
     */
    public boolean isSeasonCompleted(int userId, int seasonId) {
        List<Episode> episodes = episodeDAO.findBySeasonId(seasonId);

        for (Episode episode : episodes) {
            Optional<WatchProgress> progressOpt = watchProgressDAO.findByUserAndEpisode(userId, episode.getId());

            if (progressOpt.isEmpty() || !progressOpt.get().isCompleted()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calcule la progression globale d'une série pour un utilisateur
     * @param userId ID de l'utilisateur
     * @param serieId ID de la série
     * @return Pourcentage de complétion (0-100)
     */
    public double getSeriesCompletionPercentage(int userId, int serieId) {
        List<Episode> allEpisodes = episodeDAO.findBySerieId(serieId);

        if (allEpisodes.isEmpty()) {
            return 0.0;
        }

        long completedCount = 0;

        for (Episode episode : allEpisodes) {
            Optional<WatchProgress> progressOpt = watchProgressDAO.findByUserAndEpisode(userId, episode.getId());
            if (progressOpt.isPresent() && progressOpt.get().isCompleted()) {
                completedCount++;
            }
        }

        return (completedCount * 100.0) / allEpisodes.size();
    }

    // ==================== MÉTHODES POUR LE BINGE-WATCHING ====================

    /**
     * Vérifie si un épisode est le dernier de la série
     * @param episodeId ID de l'épisode
     * @return true si c'est le dernier épisode, false sinon
     */
    public boolean isLastEpisode(int episodeId) {
        Optional<Episode> episodeOpt = episodeDAO.findById(episodeId);

        if (episodeOpt.isEmpty()) {
            return false;
        }

        Episode episode = episodeOpt.get();

        // Récupérer tous les épisodes de la série
        List<Episode> allEpisodes = episodeDAO.findBySerieIdFromEpisode(episodeId);

        if (allEpisodes.isEmpty()) {
            return false;
        }

        // Trier par saison et numéro d'épisode
        allEpisodes.sort((e1, e2) -> {
            if (e1.getSeasonId() != e2.getSeasonId()) {
                return Integer.compare(e1.getSeasonId(), e2.getSeasonId());
            }
            return Integer.compare(e1.getEpisodeNumber(), e2.getEpisodeNumber());
        });

        Episode lastEpisode = allEpisodes.get(allEpisodes.size() - 1);

        return lastEpisode.getId() == episodeId;
    }

    /**
     * Récupère le prochain épisode à regarder après un épisode donné
     * @param currentEpisodeId ID de l'épisode actuel
     * @return Optional contenant le prochain épisode
     */
    public Optional<Episode> getNextEpisodeFromEpisode(int currentEpisodeId) {
        Optional<Episode> currentOpt = episodeDAO.findById(currentEpisodeId);

        if (currentOpt.isEmpty()) {
            return Optional.empty();
        }

        Episode current = currentOpt.get();

        // Récupérer la saison actuelle
        Optional<Season> seasonOpt = seasonDAO.findById(current.getSeasonId());

        if (seasonOpt.isEmpty()) {
            return Optional.empty();
        }

        Season season = seasonOpt.get();

        return getNextEpisode(season.getIsSerie(), season.getSeasonNumber(), current.getEpisodeNumber());
    }

    // ==================== MÉTHODES ADMIN (CRUD) ====================

    /**
     * Ajoute une nouvelle série
     * @param serie La série à ajouter
     * @return true si ajout réussi, false sinon
     */
    public boolean addSerie(Serie serie) {
        // Validation
        if (serie.getTitle() == null || serie.getTitle().trim().isEmpty()) {
            return false;
        }

        return serieDAO.insert(serie);
    }

    /**
     * Ajoute une saison à une série
     * @param season La saison à ajouter
     * @return true si ajout réussi, false sinon
     */
    public boolean addSeason(Season season) {
        // Vérifier que la série existe
        Optional<Serie> serieOpt = serieDAO.findById(season.getIsSerie());
        if (serieOpt.isEmpty()) {
            return false;
        }

        // Vérifier que le numéro de saison n'existe pas déjà
        Optional<Season> existingOpt = seasonDAO.findBySerieIdAndNumber(
                season.getIsSerie(),
                season.getSeasonNumber()
        );

        if (existingOpt.isPresent()) {
            return false; // Saison déjà existante
        }

        return seasonDAO.insert(season);
    }

    /**
     * Ajoute un épisode à une saison
     * @param episode L'épisode à ajouter
     * @return true si ajout réussi, false sinon
     */
    public boolean addEpisode(Episode episode) {
        // Vérifier que la saison existe
        Optional<Season> seasonOpt = seasonDAO.findById(episode.getSeasonId());
        if (seasonOpt.isEmpty()) {
            return false;
        }

        // Vérifier que le numéro d'épisode n'existe pas déjà dans la saison
        Optional<Episode> existingOpt = episodeDAO.findBySeasonIdAndNumber(
                episode.getSeasonId(),
                episode.getEpisodeNumber()
        );

        if (existingOpt.isPresent()) {
            return false; // Épisode déjà existant
        }

        return episodeDAO.insert(episode);
    }

    /**
     * Met à jour une série
     * @param serie La série modifiée
     * @return true si mise à jour réussie, false sinon
     */
    public boolean updateSerie(Serie serie) {
        return serieDAO.update(serie);
    }

    /**
     * Met à jour une saison
     * @param season La saison modifiée
     * @return true si mise à jour réussie, false sinon
     */
    public boolean updateSeason(Season season) {
        return seasonDAO.update(season);
    }

    /**
     * Met à jour un épisode
     * @param episode L'épisode modifié
     * @return true si mise à jour réussie, false sinon
     */
    public boolean updateEpisode(Episode episode) {
        return episodeDAO.update(episode);
    }

    /**
     * Supprime une série (soft delete)
     * @param serieId ID de la série
     * @return true si suppression réussie, false sinon
     */
    public boolean deleteSerie(int serieId) {
        return serieDAO.delete(serieId);
    }

    /**
     * Supprime une saison
     * @param seasonId ID de la saison
     * @return true si suppression réussie, false sinon
     */
    public boolean deleteSeason(int seasonId) {
        return seasonDAO.delete(seasonId);
    }

    /**
     * Supprime un épisode
     * @param episodeId ID de l'épisode
     * @return true si suppression réussie, false sinon
     */
    public boolean deleteEpisode(int episodeId) {
        return episodeDAO.delete(episodeId);
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Classe pour le résultat de reprise
     */
    public static class ResumeResult {
        private final Episode episode;
        private final double position;
        private final boolean isResume;

        public ResumeResult(Episode episode, double position, boolean isResume) {
            this.episode = episode;
            this.position = position;
            this.isResume = isResume;
        }

        public Episode getEpisode() { return episode; }
        public double getPosition() { return position; }
        public boolean isResume() { return isResume; }
    }

    /**
     * Énumération pour le statut des épisodes
     */
    public enum EpisodeStatus {
        NOT_STARTED,    // Non commencé
        IN_PROGRESS,    // En cours
        WATCHED         // Terminé
    }
}