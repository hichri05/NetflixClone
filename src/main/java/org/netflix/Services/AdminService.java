// package org.netflix.Services;

package org.netflix.Services;

import org.netflix.DAO.*;
import org.netflix.Models.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class AdminService {

    private final UserDAO userDAO;
    private final MovieDAO movieDAO;
    private final SerieDAO serieDAO;
    private final SeasonDAO seasonDAO;
    private final EpisodeDAO episodeDAO;
    private final GenreDAO genreDAO;
    private final MediaGenreDAO mediaGenreDAO;
    private final CommentDAO commentDAO;
    private final RatingDAO ratingDAO;
    private final MyListDAO myListDAO;
    private final WatchHistoryDAO watchHistoryDAO;
    private final WatchProgressDAO watchProgressDAO;

    public AdminService() {
        this.userDAO = new UserDAO();
        this.movieDAO = new MovieDAO();
        this.serieDAO = new SerieDAO();
        this.seasonDAO = new SeasonDAO();
        this.episodeDAO = new EpisodeDAO();
        this.genreDAO = new GenreDAO();
        this.mediaGenreDAO = new MediaGenreDAO();
        this.commentDAO = new CommentDAO();
        this.ratingDAO = new RatingDAO();
        this.myListDAO = new MyListDAO();
        this.watchHistoryDAO = new WatchHistoryDAO();
        this.watchProgressDAO = new WatchProgressDAO();
    }

    // ==================== VÉRIFICATION DES DROITS ADMIN ====================

    /**
     * Vérifie si un utilisateur est administrateur
     * @param userId ID de l'utilisateur
     * @return true si admin
     */
    public boolean isAdmin(int userId) {
        return userDAO.isAdmin(userId);
    }

    /**
     * Vérifie les droits admin et lève une exception si non autorisé
     * @param userId ID de l'utilisateur
     * @throws SecurityException si l'utilisateur n'est pas admin
     */
    private void checkAdminRights(int userId) {
        if (!isAdmin(userId)) {
            throw new SecurityException("Accès non autorisé. Droits administrateur requis.");
        }
    }

    // ==================== GESTION DES FILMS (CRUD) ====================

    /**
     * Ajoute un nouveau film
     * @param adminId ID de l'administrateur
     * @param film Le film à ajouter
     * @param genreIds Liste des IDs des genres à associer
     * @return true si ajout réussi
     */
    public boolean addMovie(int adminId, Movie film, List<Integer> genreIds) {
        checkAdminRights(adminId);

        // Validation des données
        if (!validateMovie(film)) {
            System.err.println("Données du film invalides");
            return false;
        }

        // Insérer le film
        boolean inserted = movieDAO.insert(film);

        if (inserted && genreIds != null) {
            // Associer les genres
            for (int genreId : genreIds) {
                mediaGenreDAO.insert(film.getIdMedia(), genreId, "MOVIE");
            }
        }

        if (inserted) {
            System.out.println("Admin " + adminId + " a ajouté le film: " + film.getTitle());
        }

        return inserted;
    }

    /**
     * Met à jour un film existant
     * @param adminId ID de l'administrateur
     * @param film Le film modifié
     * @param genreIds Liste des IDs des genres à associer
     * @return true si mise à jour réussie
     */
    public boolean updateMovie(int adminId, Movie film, List<Integer> genreIds) {
        checkAdminRights(adminId);

        if (!validateMovie(film)) {
            return false;
        }

        // Mettre à jour le film
        boolean updated = movieDAO.update(film);

        if (updated && genreIds != null) {
            // Supprimer les anciens genres
            mediaGenreDAO.deleteByMedia(film.getIdMedia(), "MOVIE");
            // Ajouter les nouveaux genres
            for (int genreId : genreIds) {
                mediaGenreDAO.insert(film.getIdMedia(), genreId, "MOVIE");
            }
        }

        if (updated) {
            System.out.println("Admin " + adminId + " a modifié le film: " + film.getTitle());
        }

        return updated;
    }

    /**
     * Supprime un film (soft delete)
     * @param adminId ID de l'administrateur
     * @param movieId ID du film
     * @return true si suppression réussie
     */
    public boolean deleteMovie(int adminId, int movieId) {
        checkAdminRights(adminId);

        Optional<Movie> movieOpt = movieDAO.findById(movieId);
        if (movieOpt.isEmpty()) {
            return false;
        }

        boolean deleted = movieDAO.delete(movieId);

        if (deleted) {
            System.out.println("Admin " + adminId + " a supprimé le film: " + movieOpt.get().getTitle());
        }

        return deleted;
    }

    // ==================== GESTION DES SÉRIES (CRUD) ====================

    /**
     * Ajoute une nouvelle série
     * @param adminId ID de l'administrateur
     * @param serie La série à ajouter
     * @param genreIds Liste des IDs des genres à associer
     * @return true si ajout réussi
     */
    public boolean addSerie(int adminId, Serie serie, List<Integer> genreIds) {
        checkAdminRights(adminId);

        if (!validateSerie(serie)) {
            return false;
        }

        boolean inserted = serieDAO.insert(serie);

        if (inserted && genreIds != null) {
            for (int genreId : genreIds) {
                mediaGenreDAO.insert(serie.getIdMedia(), genreId, "SERIE");
            }
        }

        if (inserted) {
            System.out.println("Admin " + adminId + " a ajouté la série: " + serie.getTitle());
        }

        return inserted;
    }

    /**
     * Met à jour une série existante
     * @param adminId ID de l'administrateur
     * @param serie La série modifiée
     * @param genreIds Liste des IDs des genres à associer
     * @return true si mise à jour réussie
     */
    public boolean updateSerie(int adminId, Serie serie, List<Integer> genreIds) {
        checkAdminRights(adminId);

        if (!validateSerie(serie)) {
            return false;
        }

        boolean updated = serieDAO.update(serie);

        if (updated && genreIds != null) {
            mediaGenreDAO.deleteByMedia(serie.getIdMedia(), "SERIE");
            for (int genreId : genreIds) {
                mediaGenreDAO.insert(serie.getIdMedia(), genreId, "SERIE");
            }
        }

        if (updated) {
            System.out.println("Admin " + adminId + " a modifié la série: " + serie.getTitle());
        }

        return updated;
    }

    /**
     * Supprime une série (soft delete)
     * @param adminId ID de l'administrateur
     * @param serieId ID de la série
     * @return true si suppression réussie
     */
    public boolean deleteSerie(int adminId, int serieId) {
        checkAdminRights(adminId);

        Optional<Serie> serieOpt = serieDAO.findById(serieId);
        if (serieOpt.isEmpty()) {
            return false;
        }

        boolean deleted = serieDAO.delete(serieId);

        if (deleted) {
            System.out.println("Admin " + adminId + " a supprimé la série: " + serieOpt.get().getTitle());
        }

        return deleted;
    }

    // ==================== GESTION DES SAISONS ====================

    /**
     * Ajoute une saison à une série
     * @param adminId ID de l'administrateur
     * @param season La saison à ajouter
     * @return true si ajout réussi
     */
    public boolean addSeason(int adminId, Season season) {
        checkAdminRights(adminId);

        // Vérifier que la série existe
        Optional<Serie> serieOpt = serieDAO.findById(season.getIsSerie());
        if (serieOpt.isEmpty()) {
            return false;
        }

        // Vérifier que le numéro de saison n'existe pas déjà
        Optional<Season> existing = seasonDAO.findBySerieIdAndNumber(
                season.getIsSerie(),
                season.getSeasonNumber()
        );

        if (existing.isPresent()) {
            System.err.println("La saison " + season.getSeasonNumber() + " existe déjà");
            return false;
        }

        boolean inserted = seasonDAO.insert(season);

        if (inserted) {
            // Mettre à jour le nombre de saisons dans la série
            int seasonCount = seasonDAO.countBySerieId(season.getIsSerie());
            serieDAO.updateSeasonCount(season.getIsSerie(), seasonCount);

            System.out.println("Admin " + adminId + " a ajouté la saison " + season.getSeasonNumber() +
                    " à la série: " + serieOpt.get().getTitle());
        }

        return inserted;
    }

    /**
     * Met à jour une saison
     * @param adminId ID de l'administrateur
     * @param season La saison modifiée
     * @return true si mise à jour réussie
     */
    public boolean updateSeason(int adminId, Season season) {
        checkAdminRights(adminId);
        return seasonDAO.update(season);
    }

    /**
     * Supprime une saison
     * @param adminId ID de l'administrateur
     * @param seasonId ID de la saison
     * @return true si suppression réussie
     */
    public boolean deleteSeason(int adminId, int seasonId) {
        checkAdminRights(adminId);

        Optional<Season> seasonOpt = seasonDAO.findById(seasonId);
        if (seasonOpt.isEmpty()) {
            return false;
        }

        int serieId = seasonOpt.get().getIsSerie();
        boolean deleted = seasonDAO.delete(seasonId);

        if (deleted) {
            // Mettre à jour le nombre de saisons dans la série
            int seasonCount = seasonDAO.countBySerieId(serieId);
            serieDAO.updateSeasonCount(serieId, seasonCount);

            System.out.println("Admin " + adminId + " a supprimé la saison " +
                    seasonOpt.get().getSeasonNumber());
        }

        return deleted;
    }

    // ==================== GESTION DES ÉPISODES ====================

    /**
     * Ajoute un épisode à une saison
     * @param adminId ID de l'administrateur
     * @param episode L'épisode à ajouter
     * @return true si ajout réussi
     */
    public boolean addEpisode(int adminId, Episode episode) {
        checkAdminRights(adminId);

        // Vérifier que la saison existe
        Optional<Season> seasonOpt = seasonDAO.findById(episode.getSeasonId());
        if (seasonOpt.isEmpty()) {
            return false;
        }

        // Vérifier que le numéro d'épisode n'existe pas déjà
        Optional<Episode> existing = episodeDAO.findBySeasonIdAndNumber(
                episode.getSeasonId(),
                episode.getEpisodeNumber()
        );

        if (existing.isPresent()) {
            System.err.println("L'épisode " + episode.getEpisodeNumber() + " existe déjà dans cette saison");
            return false;
        }

        boolean inserted = episodeDAO.insert(episode);

        if (inserted) {
            System.out.println("Admin " + adminId + " a ajouté l'épisode " + episode.getEpisodeNumber() +
                    " à la saison " + seasonOpt.get().getSeasonNumber());
        }

        return inserted;
    }

    /**
     * Met à jour un épisode
     * @param adminId ID de l'administrateur
     * @param episode L'épisode modifié
     * @return true si mise à jour réussie
     */
    public boolean updateEpisode(int adminId, Episode episode) {
        checkAdminRights(adminId);
        return episodeDAO.update(episode);
    }

    /**
     * Supprime un épisode
     * @param adminId ID de l'administrateur
     * @param episodeId ID de l'épisode
     * @return true si suppression réussie
     */
    public boolean deleteEpisode(int adminId, int episodeId) {
        checkAdminRights(adminId);

        Optional<Episode> episodeOpt = episodeDAO.findById(episodeId);
        if (episodeOpt.isEmpty()) {
            return false;
        }

        boolean deleted = episodeDAO.delete(episodeId);

        if (deleted) {
            System.out.println("Admin " + adminId + " a supprimé l'épisode: " + episodeOpt.get().getTitle());
        }

        return deleted;
    }

    // ==================== GESTION DES GENRES ====================

    /**
     * Ajoute un nouveau genre
     * @param adminId ID de l'administrateur
     * @param genre Le genre à ajouter
     * @return true si ajout réussi
     */
    public boolean addGenre(int adminId, Genre genre) {
        checkAdminRights(adminId);

        if (genre.getName() == null || genre.getName().trim().isEmpty()) {
            return false;
        }

        // Vérifier que le genre n'existe pas déjà
        Optional<Genre> existing = genreDAO.findByName(genre.getName());
        if (existing.isPresent()) {
            System.err.println("Le genre " + genre.getName() + " existe déjà");
            return false;
        }

        boolean inserted = genreDAO.insert(genre);

        if (inserted) {
            System.out.println("Admin " + adminId + " a ajouté le genre: " + genre.getName());
        }

        return inserted;
    }

    /**
     * Met à jour un genre
     * @param adminId ID de l'administrateur
     * @param genre Le genre modifié
     * @return true si mise à jour réussie
     */
    public boolean updateGenre(int adminId, Genre genre) {
        checkAdminRights(adminId);
        return genreDAO.update(genre);
    }

    /**
     * Supprime un genre
     * @param adminId ID de l'administrateur
     * @param genreId ID du genre
     * @return true si suppression réussie
     */
    public boolean deleteGenre(int adminId, int genreId) {
        checkAdminRights(adminId);

        Optional<Genre> genreOpt = genreDAO.findById(genreId);
        if (genreOpt.isEmpty()) {
            return false;
        }

        boolean deleted = genreDAO.delete(genreId);

        if (deleted) {
            System.out.println("Admin " + adminId + " a supprimé le genre: " + genreOpt.get().getName());
        }

        return deleted;
    }

    // ==================== MODÉRATION DES COMMENTAIRES ====================

    /**
     * Récupère tous les commentaires signalés
     * @param adminId ID de l'administrateur
     * @return Liste des commentaires signalés
     */
    public List<Comment> getReportedComments(int adminId) {
        checkAdminRights(adminId);
        return commentDAO.findReportedComments();
    }

    /**
     * Approuve un commentaire signalé (retire le signalement)
     * @param adminId ID de l'administrateur
     * @param commentId ID du commentaire
     * @return true si approbation réussie
     */
    public boolean approveComment(int adminId, int commentId) {
        checkAdminRights(adminId);
        return commentDAO.unmarkAsReported(commentId);
    }

    /**
     * Supprime un commentaire (modération)
     * @param adminId ID de l'administrateur
     * @param commentId ID du commentaire
     * @return true si suppression réussie
     */
    public boolean deleteComment(int adminId, int commentId) {
        checkAdminRights(adminId);
        return commentDAO.delete(commentId);
    }

    /**
     * Supprime tous les commentaires signalés
     * @param adminId ID de l'administrateur
     * @return Nombre de commentaires supprimés
     */
    public int deleteAllReportedComments(int adminId) {
        checkAdminRights(adminId);
        return commentDAO.deleteReportedComments();
    }

    // ==================== GESTION DES UTILISATEURS ====================

    /**
     * Récupère tous les utilisateurs
     * @param adminId ID de l'administrateur
     * @return Liste des utilisateurs
     */
    public List<User> getAllUsers(int adminId) {
        checkAdminRights(adminId);
        return userDAO.findAll();
    }

    /**
     * Désactive un utilisateur (bannissement)
     * @param adminId ID de l'administrateur
     * @param userId ID de l'utilisateur à désactiver
     * @return true si désactivation réussie
     */
    public boolean disableUser(int adminId, int userId) {
        checkAdminRights(adminId);

        // Un admin ne peut pas se désactiver lui-même
        if (adminId == userId) {
            System.err.println("Un administrateur ne peut pas se désactiver lui-même");
            return false;
        }

        return userDAO.setActive(userId, false);
    }

    /**
     * Active un utilisateur
     * @param adminId ID de l'administrateur
     * @param userId ID de l'utilisateur à activer
     * @return true si activation réussie
     */
    public boolean enableUser(int adminId, int userId) {
        checkAdminRights(adminId);
        return userDAO.setActive(userId, true);
    }

    /**
     * Change le rôle d'un utilisateur
     * @param adminId ID de l'administrateur
     * @param userId ID de l'utilisateur
     * @param isAdmin true pour donner les droits admin, false pour retirer
     * @return true si changement réussi
     */
    public boolean setUserRole(int adminId, int userId, boolean isAdmin) {
        checkAdminRights(adminId);

        // Un admin ne peut pas modifier son propre rôle
        if (adminId == userId) {
            System.err.println("Un administrateur ne peut pas modifier son propre rôle");
            return false;
        }

        String role = isAdmin ? "ADMIN" : "USER";
        return userDAO.updateRole(userId, role);
    }

    // ==================== MÉTHODES DE VALIDATION ====================

    /**
     * Valide les données d'un film
     */
    private boolean validateMovie(Movie film) {
        if (film.getTitle() == null || film.getTitle().trim().isEmpty()) {
            return false;
        }
        if (film.getDurationMinutes() <= 0) {
            return false;
        }
        if (film.getReleaseYear() < 1900 || film.getReleaseYear() > LocalDateTime.now().getYear() + 1) {
            return false;
        }
        return true;
    }

    /**
     * Valide les données d'une série
     */
    private boolean validateSerie(Serie serie) {
        if (serie.getTitle() == null || serie.getTitle().trim().isEmpty()) {
            return false;
        }
        if (serie.getReleaseYear() < 1900 || serie.getReleaseYear() > LocalDateTime.now().getYear() + 1) {
            return false;
        }
        return true;
    }

    // ==================== STATISTIQUES ADMIN ====================

    /**
     * Récupère les statistiques générales de la plateforme
     * @param adminId ID de l'administrateur
     * @return Objet contenant les statistiques
     */
    public AdminStatistics getStatistics(int adminId) {
        checkAdminRights(adminId);

        return new AdminStatistics(
                userDAO.countAll(),
                userDAO.countAdmins(),
                movieDAO.count(),
                serieDAO.count(),
                commentDAO.countAll(),
                commentDAO.countReported(),
                ratingDAO.countAll(),
                watchHistoryDAO.countAll()
        );
    }

    /**
     * Récupère l'activité récente (derniers commentaires, inscriptions, etc.)
     * @param adminId ID de l'administrateur
     * @param limit Nombre maximum d'éléments
     * @return Liste des activités récentes
     */
    public List<RecentActivity> getRecentActivity(int adminId, int limit) {
        checkAdminRights(adminId);

        List<RecentActivity> activities = new ArrayList<>();

        // Derniers utilisateurs inscrits
        List<User> recentUsers = userDAO.findRecent(limit);
        for (User user : recentUsers) {
            activities.add(new RecentActivity(
                    "USER_REGISTERED",
                    user.getUsername() + " s'est inscrit",
                    user.getCreatedAt(),
                    user.getId()
            ));
        }

        // Derniers commentaires
        List<Comment> recentComments = commentDAO.findRecentComments(limit);
        for (Comment comment : recentComments) {
            activities.add(new RecentActivity(
                    "COMMENT_ADDED",
                    "Nouveau commentaire sur le média " + comment.getMediaId(),
                    comment.getCreatedAt(),
                    comment.getUserId()
            ));
        }

        // Trier par date
        activities.sort((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()));

        if (activities.size() > limit) {
            return activities.subList(0, limit);
        }
        return activities;
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Classe pour les statistiques administratives
     */
    public static class AdminStatistics {
        private final int totalUsers;
        private final int totalAdmins;
        private final int totalMovies;
        private final int totalSeries;
        private final int totalComments;
        private final int reportedComments;
        private final int totalRatings;
        private final int totalWatchHistory;

        public AdminStatistics(int totalUsers, int totalAdmins, int totalMovies, int totalSeries,
                               int totalComments, int reportedComments, int totalRatings, int totalWatchHistory) {
            this.totalUsers = totalUsers;
            this.totalAdmins = totalAdmins;
            this.totalMovies = totalMovies;
            this.totalSeries = totalSeries;
            this.totalComments = totalComments;
            this.reportedComments = reportedComments;
            this.totalRatings = totalRatings;
            this.totalWatchHistory = totalWatchHistory;
        }

        public int getTotalUsers() { return totalUsers; }
        public int getTotalAdmins() { return totalAdmins; }
        public int getTotalMovies() { return totalMovies; }
        public int getTotalSeries() { return totalSeries; }
        public int getTotalContent() { return totalMovies + totalSeries; }
        public int getTotalComments() { return totalComments; }
        public int getReportedComments() { return reportedComments; }
        public int getTotalRatings() { return totalRatings; }
        public int getTotalWatchHistory() { return totalWatchHistory; }
    }

    /**
     * Classe pour l'activité récente
     */
    public static class RecentActivity {
        private final String type;
        private final String description;
        private final Timestamp timestamp;
        private final int userId;

        public RecentActivity(String type, String description, Timestamp timestamp, int userId) {
            this.type = type;
            this.description = description;
            this.timestamp = timestamp;
            this.userId = userId;
        }

        public String getType() { return type; }
        public String getDescription() { return description; }
        public Timestamp getTimestamp() { return timestamp; }
        public int getUserId() { return userId; }

        public String getTimeAgo() {
            long diff = System.currentTimeMillis() - timestamp.getTime();
            long minutes = diff / (60 * 1000);
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) return days + " jour" + (days > 1 ? "s" : "");
            if (hours > 0) return hours + " heure" + (hours > 1 ? "s" : "");
            if (minutes > 0) return minutes + " minute" + (minutes > 1 ? "s" : "");
            return "À l'instant";
        }
    }
}