// package org.netflix.Services;

package org.netflix.Services;

import org.netflix.DAO.RatingDAO;
import org.netflix.DAO.MovieDAO;
import org.netflix.DAO.SerieDAO;
import org.netflix.Models.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class RatingService {

    private final RatingDAO ratingDAO;
    private final MovieDAO movieDAO;
    private final SerieDAO serieDAO;

    public RatingService() {
        this.ratingDAO = new RatingDAO();
        this.movieDAO = new MovieDAO();
        this.serieDAO = new SerieDAO();
    }

    // ==================== NOTATION D'UN MÉDIA ====================

    /**
     * Note un média (film ou série)
     * @param userId ID de l'utilisateur
     * @param mediaId ID du média
     * @param ratingValue Note de 1 à 5
     * @return true si la note a été enregistrée, false sinon
     */
    public boolean rateMedia(int userId, int mediaId, int ratingValue) {
        // 1. Validation de la note
        if (!isValidRating(ratingValue)) {
            System.err.println("Note invalide: " + ratingValue + ". La note doit être entre 1 et 5");
            return false;
        }

        // 2. Vérifier que le média existe (film ou série)
        if (!mediaExists(mediaId)) {
            System.err.println("Média non trouvé: " + mediaId);
            return false;
        }

        // 3. Vérifier si l'utilisateur a déjà noté ce média
        Optional<Rating> existingRating = ratingDAO.findByUserAndMedia(userId, mediaId);

        boolean success;
        if (existingRating.isPresent()) {
            // Mettre à jour la note existante
            Rating rating = existingRating.get();
            rating.setRating(ratingValue);
            rating.setRatedAt(Timestamp.valueOf(LocalDateTime.now()));
            success = ratingDAO.update(rating);
        } else {
            // Ajouter une nouvelle note
            Rating rating = new Rating(
                    0,
                    userId,
                    mediaId,
                    ratingValue,
                    Timestamp.valueOf(LocalDateTime.now())
            );
            success = ratingDAO.insert(rating);
        }

        // 4. Recalculer et mettre à jour la note moyenne du média
        if (success) {
            updateMediaAverageRating(mediaId);
        }

        return success;
    }

    /**
     * Supprime la note d'un utilisateur pour un média
     * @param userId ID de l'utilisateur
     * @param mediaId ID du média
     * @return true si suppression réussie
     */
    public boolean removeRating(int userId, int mediaId) {
        Optional<Rating> ratingOpt = ratingDAO.findByUserAndMedia(userId, mediaId);

        if (ratingOpt.isEmpty()) {
            return false;
        }

        boolean success = ratingDAO.delete(ratingOpt.get().getId());

        // Recalculer la note moyenne après suppression
        if (success) {
            updateMediaAverageRating(mediaId);
        }

        return success;
    }

    // ==================== RÉCUPÉRATION DES NOTES ====================

    /**
     * Récupère la note d'un utilisateur pour un média
     * @param userId ID de l'utilisateur
     * @param mediaId ID du média
     * @return Optional contenant la note (1-5) ou vide si non noté
     */
    public Optional<Integer> getUserRating(int userId, int mediaId) {
        Optional<Rating> ratingOpt = ratingDAO.findByUserAndMedia(userId, mediaId);
        return ratingOpt.map(Rating::getRating);
    }

    /**
     * Récupère la note moyenne d'un média
     * @param mediaId ID du média
     * @return Note moyenne (0-5)
     */
    public double getAverageRating(int mediaId) {
        return ratingDAO.calculateAverageRating(mediaId);
    }

    /**
     * Récupère le nombre total de notes pour un média
     * @param mediaId ID du média
     * @return Nombre de notes
     */
    public int getRatingCount(int mediaId) {
        return ratingDAO.countByMedia(mediaId);
    }

    /**
     * Récupère la distribution des notes pour un média
     * @param mediaId ID du média
     * @return Map contenant pour chaque note (1-5) le nombre de votes
     */
    public Map<Integer, Integer> getRatingDistribution(int mediaId) {
        return ratingDAO.getRatingDistribution(mediaId);
    }

    /**
     * Récupère tous les médias notés par un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des médias avec leurs notes
     */
    public List<RatedMedia> getRatedMediaByUser(int userId) {
        List<Rating> ratings = ratingDAO.findByUser(userId);
        List<RatedMedia> ratedMediaList = new ArrayList<>();

        for (Rating rating : ratings) {
            int mediaId = rating.getMediaId();
            String mediaTitle = getMediaTitle(mediaId);
            String mediaType = getMediaType(mediaId);
            String mediaCover = getMediaCover(mediaId);

            ratedMediaList.add(new RatedMedia(
                    mediaId,
                    mediaTitle,
                    mediaType,
                    mediaCover,
                    rating.getRating(),
                    rating.getRatedAt()
            ));
        }

        // Trier par date de notation (plus récent d'abord)
        ratedMediaList.sort((r1, r2) -> r2.getRatedAt().compareTo(r1.getRatedAt()));

        return ratedMediaList;
    }

    // ==================== TOP ET CLASSEMENTS ====================

    /**
     * Récupère le top N des médias les mieux notés
     * @param limit Nombre maximum de résultats
     * @return Liste des médias avec leurs notes moyennes
     */
    public List<TopRatedMedia> getTopRatedMedia(int limit) {
        return ratingDAO.getTopRatedMedia(limit);
    }

    /**
     * Récupère le top N des films les mieux notés
     * @param limit Nombre maximum de résultats
     * @return Liste des films avec leurs notes moyennes
     */
    public List<TopRatedMedia> getTopRatedMovies(int limit) {
        return ratingDAO.getTopRatedMovies(limit);
    }

    /**
     * Récupère le top N des séries les mieux notées
     * @param limit Nombre maximum de résultats
     * @return Liste des séries avec leurs notes moyennes
     */
    public List<TopRatedMedia> getTopRatedSeries(int limit) {
        return ratingDAO.getTopRatedSeries(limit);
    }

    /**
     * Récupère les médias récemment notés
     * @param limit Nombre maximum de résultats
     * @return Liste des dernières notes
     */
    public List<RecentRating> getRecentRatings(int limit) {
        List<Rating> ratings = ratingDAO.findRecentRatings(limit);
        List<RecentRating> recentRatings = new ArrayList<>();

        for (Rating rating : ratings) {
            String username = getUserName(rating.getUserId());
            String mediaTitle = getMediaTitle(rating.getMediaId());

            recentRatings.add(new RecentRating(
                    rating.getUserId(),
                    username,
                    rating.getMediaId(),
                    mediaTitle,
                    rating.getRating(),
                    rating.getRatedAt()
            ));
        }

        return recentRatings;
    }

    // ==================== MÉTHODES DE VALIDATION ====================

    /**
     * Vérifie si une note est valide (1-5)
     * @param rating Note à vérifier
     * @return true si valide
     */
    public boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    /**
     * Vérifie si un utilisateur a déjà noté un média
     * @param userId ID de l'utilisateur
     * @param mediaId ID du média
     * @return true si déjà noté
     */
    public boolean hasUserRated(int userId, int mediaId) {
        return ratingDAO.findByUserAndMedia(userId, mediaId).isPresent();
    }

    // ==================== MÉTHODES DE MISE À JOUR ====================

    /**
     * Met à jour la note moyenne d'un média dans sa table respective
     * @param mediaId ID du média
     */
    private void updateMediaAverageRating(int mediaId) {
        double averageRating = ratingDAO.calculateAverageRating(mediaId);

        // Vérifier si c'est un film ou une série
        Optional<Movie> movieOpt = movieDAO.findById(mediaId);
        if (movieOpt.isPresent()) {
            Movie movie = movieOpt.get();
            movie.setAverageRating(averageRating);
            movieDAO.updateRating(movie);
            return;
        }

        Optional<Serie> serieOpt = serieDAO.findById(mediaId);
        if (serieOpt.isPresent()) {
            Serie serie = serieOpt.get();
            serie.setAverageRating(averageRating);
            serieDAO.updateRating(serie);
        }
    }

    /**
     * Vérifie si un média existe (film ou série)
     * @param mediaId ID du média
     * @return true si existe
     */
    private boolean mediaExists(int mediaId) {
        return movieDAO.findById(mediaId).isPresent() ||
                serieDAO.findById(mediaId).isPresent();
    }

    /**
     * Récupère le titre d'un média
     * @param mediaId ID du média
     * @return Titre du média
     */
    private String getMediaTitle(int mediaId) {
        Optional<Movie> movieOpt = movieDAO.findById(mediaId);
        if (movieOpt.isPresent()) {
            return movieOpt.get().getTitle();
        }

        Optional<Serie> serieOpt = serieDAO.findById(mediaId);
        if (serieOpt.isPresent()) {
            return serieOpt.get().getTitle();
        }

        return "Média inconnu";
    }

    /**
     * Récupère le type d'un média (MOVIE ou SERIE)
     * @param mediaId ID du média
     * @return Type du média
     */
    private String getMediaType(int mediaId) {
        if (movieDAO.findById(mediaId).isPresent()) {
            return "MOVIE";
        }
        if (serieDAO.findById(mediaId).isPresent()) {
            return "SERIE";
        }
        return "UNKNOWN";
    }

    /**
     * Récupère l'URL de couverture d'un média
     * @param mediaId ID du média
     * @return URL de la couverture
     */
    private String getMediaCover(int mediaId) {
        Optional<Movie> movieOpt = movieDAO.findById(mediaId);
        if (movieOpt.isPresent()) {
            return movieOpt.get().getCoverImageUrl();
        }

        Optional<Serie> serieOpt = serieDAO.findById(mediaId);
        if (serieOpt.isPresent()) {
            return serieOpt.get().getCoverImageUrl();
        }

        return "";
    }

    /**
     * Récupère le nom d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Nom de l'utilisateur
     */
    private String getUserName(int userId) {
        // À implémenter avec UserDAO
        // Pour l'instant, retourne un placeholder
        return "Utilisateur " + userId;
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Classe pour représenter un média noté par un utilisateur
     */
    public static class RatedMedia {
        private final int mediaId;
        private final String title;
        private final String type;
        private final String coverImageUrl;
        private final int rating;
        private final Timestamp ratedAt;

        public RatedMedia(int mediaId, String title, String type, String coverImageUrl,
                          int rating, Timestamp ratedAt) {
            this.mediaId = mediaId;
            this.title = title;
            this.type = type;
            this.coverImageUrl = coverImageUrl;
            this.rating = rating;
            this.ratedAt = ratedAt;
        }

        public int getMediaId() { return mediaId; }
        public String getTitle() { return title; }
        public String getType() { return type; }
        public String getCoverImageUrl() { return coverImageUrl; }
        public int getRating() { return rating; }
        public Timestamp getRatedAt() { return ratedAt; }

        public String getStarRating() {
            return "★".repeat(rating) + "☆".repeat(5 - rating);
        }
    }

    /**
     * Classe pour représenter un média dans le top des mieux notés
     */
    public static class TopRatedMedia {
        private final int mediaId;
        private final String title;
        private final String type;
        private final String coverImageUrl;
        private final double averageRating;
        private final int ratingCount;

        public TopRatedMedia(int mediaId, String title, String type, String coverImageUrl,
                             double averageRating, int ratingCount) {
            this.mediaId = mediaId;
            this.title = title;
            this.type = type;
            this.coverImageUrl = coverImageUrl;
            this.averageRating = averageRating;
            this.ratingCount = ratingCount;
        }

        public int getMediaId() { return mediaId; }
        public String getTitle() { return title; }
        public String getType() { return type; }
        public String getCoverImageUrl() { return coverImageUrl; }
        public double getAverageRating() { return averageRating; }
        public int getRatingCount() { return ratingCount; }

        public String getFormattedRating() {
            return String.format("%.1f", averageRating);
        }

        public String getStarRating() {
            int fullStars = (int) Math.round(averageRating);
            return "★".repeat(fullStars) + "☆".repeat(5 - fullStars);
        }
    }

    /**
     * Classe pour représenter une note récente
     */
    public static class RecentRating {
        private final int userId;
        private final String username;
        private final int mediaId;
        private final String mediaTitle;
        private final int rating;
        private final Timestamp ratedAt;

        public RecentRating(int userId, String username, int mediaId, String mediaTitle,
                            int rating, Timestamp ratedAt) {
            this.userId = userId;
            this.username = username;
            this.mediaId = mediaId;
            this.mediaTitle = mediaTitle;
            this.rating = rating;
            this.ratedAt = ratedAt;
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public int getMediaId() { return mediaId; }
        public String getMediaTitle() { return mediaTitle; }
        public int getRating() { return rating; }
        public Timestamp getRatedAt() { return ratedAt; }

        public String getStarRating() {
            return "★".repeat(rating) + "☆".repeat(5 - rating);
        }

        public String getTimeAgo() {
            long diff = System.currentTimeMillis() - ratedAt.getTime();
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