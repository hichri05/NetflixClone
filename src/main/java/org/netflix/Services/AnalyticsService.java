// package org.netflix.Services;

package org.netflix.Services;

import org.netflix.DAO.*;
import org.netflix.Models.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsService {

    private final UserDAO userDAO;
    private final MovieDAO movieDAO;
    private final SerieDAO serieDAO;
    private final RatingDAO ratingDAO;
    private final CommentDAO commentDAO;
    private final WatchHistoryDAO watchHistoryDAO;
    private final WatchProgressDAO watchProgressDAO;
    private final MyListDAO myListDAO;
    private final GenreDAO genreDAO;


    public AnalyticsService() {
        this.userDAO = new UserDAO();
        this.movieDAO = new MovieDAO();
        this.serieDAO = new SerieDAO();
        this.ratingDAO = new RatingDAO();
        this.commentDAO = new CommentDAO();
        this.watchHistoryDAO = new WatchHistoryDAO();
        this.watchProgressDAO = new WatchProgressDAO();
        this.myListDAO = new MyListDAO();
        this.genreDAO = new GenreDAO();

    }

    public Map<String, Integer> getMoviesDistributionByGenre() {
        List<Genre> allGenres = genreDAO.findAll();
        Map<String, Integer> distribution = new LinkedHashMap<>();

        for (Genre genre : allGenres) {
            int count = movieDAO.countByGenre(genre.getId());
            if (count > 0) {
                distribution.put(genre.getName(), count);
            }
        }

        // Trier par nombre décroissant
        return distribution.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Récupère la répartition des séries par genre
     * @return Map contenant le nom du genre et le nombre de séries
     */
    public Map<String, Integer> getSeriesDistributionByGenre() {
        List<Genre> allGenres = genreDAO.findAll();
        Map<String, Integer> distribution = new LinkedHashMap<>();

        for (Genre genre : allGenres) {
            int count = serieDAO.countByGenre(genre.getId());
            if (count > 0) {
                distribution.put(genre.getName(), count);
            }
        }

        return distribution.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Récupère la répartition globale (films + séries) par genre
     * @return Map contenant le nom du genre et le nombre total de contenus
     */
    public Map<String, Integer> getGlobalDistributionByGenre() {
        Map<String, Integer> moviesDist = getMoviesDistributionByGenre();
        Map<String, Integer> seriesDist = getSeriesDistributionByGenre();

        Map<String, Integer> globalDist = new LinkedHashMap<>();

        // Fusionner les deux maps
        Set<String> allGenres = new HashSet<>();
        allGenres.addAll(moviesDist.keySet());
        allGenres.addAll(seriesDist.keySet());

        for (String genre : allGenres) {
            int movieCount = moviesDist.getOrDefault(genre, 0);
            int serieCount = seriesDist.getOrDefault(genre, 0);
            globalDist.put(genre, movieCount + serieCount);
        }

        // Trier par nombre décroissant
        return globalDist.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    // ==================== TOP 5 DES FILMS LES PLUS VUS ====================

    /**
     * Récupère le top N des films les plus vus (basé sur watch_history)
     * @param limit Nombre maximum de résultats
     * @return Liste des films avec leur nombre de vues
     */
    public List<TopViewedMedia> getTopViewedMovies(int limit) {
        List<Object[]> results = watchHistoryDAO.getTopViewedMovies(limit);
        List<TopViewedMedia> topMovies = new ArrayList<>();

        for (Object[] row : results) {
            int movieId = (int) row[0];
            long viewCount = (long) row[1];

            Optional<Movie> movieOpt = movieDAO.findById(movieId);
            if (movieOpt.isPresent()) {
                Movie movie = movieOpt.get();
                topMovies.add(new TopViewedMedia(
                        movieId,
                        movie.getTitle(),
                        movie.getCoverImageUrl(),
                        "MOVIE",
                        viewCount,
                        movie.getAverageRating()
                ));
            }
        }

        return topMovies;
    }

    /**
     * Récupère le top N des séries les plus vues
     * @param limit Nombre maximum de résultats
     * @return Liste des séries avec leur nombre de vues
     */
    public List<TopViewedMedia> getTopViewedSeries(int limit) {
        List<Object[]> results = watchHistoryDAO.getTopViewedSeries(limit);
        List<TopViewedMedia> topSeries = new ArrayList<>();

        for (Object[] row : results) {
            int serieId = (int) row[0];
            long viewCount = (long) row[1];

            Optional<Serie> serieOpt = serieDAO.findById(serieId);
            if (serieOpt.isPresent()) {
                Serie serie = serieOpt.get();
                topSeries.add(new TopViewedMedia(
                        serieId,
                        serie.getTitle(),
                        serie.getCoverImageUrl(),
                        "SERIE",
                        viewCount,
                        serie.getAverageRating()
                ));
            }
        }

        return topSeries;
    }

    /**
     * Récupère le top N des contenus (films + séries) les plus vus
     * @param limit Nombre maximum de résultats
     * @return Liste des contenus avec leur nombre de vues
     */
    public List<TopViewedMedia> getTopViewedContent(int limit) {
        List<TopViewedMedia> allContent = new ArrayList<>();
        allContent.addAll(getTopViewedMovies(limit));
        allContent.addAll(getTopViewedSeries(limit));

        // Trier par nombre de vues
        allContent.sort((c1, c2) -> Long.compare(c2.getViewCount(), c1.getViewCount()));

        if (allContent.size() > limit) {
            return allContent.subList(0, limit);
        }
        return allContent;
    }

    // ==================== NOMBRE D'INSCRITS PAR JOUR ====================

    /**
     * Récupère le nombre d'inscrits par jour pour une période donnée
     * @param days Nombre de jours à remonter
     * @return Map contenant la date et le nombre d'inscriptions
     */
    public Map<String, Integer> getRegistrationsPerDay(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        Map<String, Integer> registrations = new LinkedHashMap<>();

        // Initialiser la map avec toutes les dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            registrations.put(date.format(formatter), 0);
        }

        // Remplir avec les données réelles
        List<Object[]> results = userDAO.getRegistrationsPerDay(startDate, endDate);

        for (Object[] row : results) {
            String date = row[0].toString();
            int count = ((Long) row[1]).intValue();
            registrations.put(date, count);
        }

        return registrations;
    }

    /**
     * Récupère le nombre d'inscrits par mois
     * @param months Nombre de mois à remonter
     * @return Map contenant le mois et le nombre d'inscriptions
     */
    public Map<String, Integer> getRegistrationsPerMonth(int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months - 1);

        Map<String, Integer> registrations = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusMonths(1)) {
            registrations.put(date.format(formatter), 0);
        }

        List<Object[]> results = userDAO.getRegistrationsPerMonth(startDate, endDate);

        for (Object[] row : results) {
            String month = row[0].toString();
            int count = ((Long) row[1]).intValue();
            registrations.put(month, count);
        }

        return registrations;
    }

    // ==================== STATISTIQUES GLOBALES ====================

    /**
     * Récupère les statistiques globales de la plateforme
     * @return Objet contenant toutes les statistiques
     */
    public GlobalStatistics getGlobalStatistics() {
        return new GlobalStatistics(
                userDAO.countAll(),
                userDAO.countActiveUsers(),
                movieDAO.count(),
                serieDAO.count(),
                ratingDAO.countAll(),
                commentDAO.countAll(),
                watchHistoryDAO.countAll(),
                myListDAO.countAll()
        );
    }

    /**
     * Récupère les statistiques d'engagement des utilisateurs
     * @return Objet contenant les statistiques d'engagement
     */
    public EngagementStatistics getEngagementStatistics() {
        double averageRatingsPerUser = ratingDAO.getAverageRatingsPerUser();
        double averageCommentsPerUser = commentDAO.getAverageCommentsPerUser();
        double averageWatchTimePerUser = watchHistoryDAO.getAverageWatchTimePerUser();

        return new EngagementStatistics(
                averageRatingsPerUser,
                averageCommentsPerUser,
                averageWatchTimePerUser,
                getMostActiveUsers(5)
        );
    }

    // ==================== STATISTIQUES PAR MÉDIA ====================

    /**
     * Récupère les statistiques détaillées pour un média spécifique
     * @param mediaId ID du média
     * @param type Type du média ("MOVIE" ou "SERIE")
     * @return Objet contenant les statistiques du média
     */
    public MediaStatistics getMediaStatistics(int mediaId, String type) {
        int totalRatings = ratingDAO.countByMedia(mediaId);
        double averageRating = ratingDAO.calculateAverageRating(mediaId);
        Map<Integer, Integer> ratingDistribution = ratingDAO.getRatingDistribution(mediaId);

        int totalComments = commentDAO.countByMedia(mediaId);
        int totalViews = watchHistoryDAO.countByMedia(mediaId, type);

        int totalInLists = myListDAO.countByMedia(mediaId, type);

        String title = getMediaTitle(mediaId, type);

        return new MediaStatistics(
                mediaId,
                title,
                type,
                totalRatings,
                averageRating,
                ratingDistribution,
                totalComments,
                totalViews,
                totalInLists
        );
    }

    // ==================== STATISTIQUES DE VISIONNAGE ====================

    /**
     * Récupère les statistiques de visionnage par heure (pour analyser les heures de pointe)
     * @return Map contenant l'heure et le nombre de visionnages
     */
    public Map<Integer, Integer> getViewingByHour() {
        List<Object[]> results = watchHistoryDAO.getViewingByHour();
        Map<Integer, Integer> viewingByHour = new LinkedHashMap<>();

        // Initialiser toutes les heures à 0
        for (int i = 0; i < 24; i++) {
            viewingByHour.put(i, 0);
        }

        for (Object[] row : results) {
            int hour = (int) row[0];
            long count = (long) row[1];
            viewingByHour.put(hour, (int) count);
        }

        return viewingByHour;
    }

    /**
     * Récupère les statistiques de visionnage par jour de la semaine
     * @return Map contenant le jour et le nombre de visionnages
     */
    public Map<String, Integer> getViewingByDayOfWeek() {
        List<Object[]> results = watchHistoryDAO.getViewingByDayOfWeek();
        Map<String, Integer> viewingByDay = new LinkedHashMap<>();

        String[] days = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        for (String day : days) {
            viewingByDay.put(day, 0);
        }

        for (Object[] row : results) {
            int dayIndex = (int) row[0];
            long count = (long) row[1];
            if (dayIndex >= 1 && dayIndex <= 7) {
                viewingByDay.put(days[dayIndex - 1], (int) count);
            }
        }

        return viewingByDay;
    }

    // ==================== STATISTIQUES DE NOTATION ====================

    /**
     * Récupère la distribution globale des notes (1-5 étoiles)
     * @return Map contenant la note et le nombre de votes
     */
    public Map<Integer, Integer> getGlobalRatingDistribution() {
        Map<Integer, Integer> distribution = new LinkedHashMap<>();

        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0);
        }

        List<Object[]> results = ratingDAO.getGlobalRatingDistribution();

        for (Object[] row : results) {
            int rating = (int) row[0];
            long count = (long) row[1];
            distribution.put(rating, (int) count);
        }

        return distribution;
    }

    // ==================== ANALYSE DES TENDANCES ====================

    /**
     * Analyse les tendances des notes au fil du temps
     * @param days Nombre de jours à analyser
     * @return Map contenant la date et la note moyenne
     */
    public Map<String, Double> getRatingTrend(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        Map<String, Double> trend = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            trend.put(date.format(formatter), 0.0);
        }

        List<Object[]> results = ratingDAO.getRatingTrend(startDate, endDate);

        for (Object[] row : results) {
            String date = row[0].toString();
            double avgRating = (double) row[1];
            trend.put(date, avgRating);
        }

        return trend;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Récupère les utilisateurs les plus actifs
     * @param limit Nombre maximum de résultats
     * @return Liste des utilisateurs avec leur niveau d'activité
     */
    private List<ActiveUser> getMostActiveUsers(int limit) {
        List<Object[]> results = userDAO.getMostActiveUsers(limit);
        List<ActiveUser> activeUsers = new ArrayList<>();

        for (Object[] row : results) {
            int userId = (int) row[0];
            String username = (String) row[1];
            long activityCount = (long) row[2];

            activeUsers.add(new ActiveUser(userId, username, activityCount));
        }

        return activeUsers;
    }

    /**
     * Récupère le titre d'un média
     */
    private String getMediaTitle(int mediaId, String type) {
        if ("MOVIE".equalsIgnoreCase(type)) {
            Optional<Movie> movieOpt = movieDAO.findById(mediaId);
            return movieOpt.map(Movie::getTitle).orElse("Média inconnu");
        } else {
            Optional<Serie> serieOpt = serieDAO.findById(mediaId);
            return serieOpt.map(Serie::getTitle).orElse("Média inconnu");
        }
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Classe pour les contenus les plus vus
     */
    public static class TopViewedMedia {
        private final int id;
        private final String title;
        private final String coverImageUrl;
        private final String type;
        private final long viewCount;
        private final double averageRating;

        public TopViewedMedia(int id, String title, String coverImageUrl, String type,
                              long viewCount, double averageRating) {
            this.id = id;
            this.title = title;
            this.coverImageUrl = coverImageUrl;
            this.type = type;
            this.viewCount = viewCount;
            this.averageRating = averageRating;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getCoverImageUrl() { return coverImageUrl; }
        public String getType() { return type; }
        public long getViewCount() { return viewCount; }
        public double getAverageRating() { return averageRating; }

        public String getFormattedViewCount() {
            if (viewCount >= 1_000_000) {
                return String.format("%.1fM", viewCount / 1_000_000.0);
            } else if (viewCount >= 1_000) {
                return String.format("%.1fk", viewCount / 1_000.0);
            }
            return String.valueOf(viewCount);
        }

        public String getStarRating() {
            int fullStars = (int) Math.round(averageRating);
            return "★".repeat(fullStars) + "☆".repeat(5 - fullStars);
        }
    }

    /**
     * Classe pour les statistiques globales
     */
    public static class GlobalStatistics {
        private final int totalUsers;
        private final int activeUsers;
        private final int totalMovies;
        private final int totalSeries;
        private final int totalRatings;
        private final int totalComments;
        private final int totalViews;
        private final int totalInLists;

        public GlobalStatistics(int totalUsers, int activeUsers, int totalMovies, int totalSeries,
                                int totalRatings, int totalComments, int totalViews, int totalInLists) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.totalMovies = totalMovies;
            this.totalSeries = totalSeries;
            this.totalRatings = totalRatings;
            this.totalComments = totalComments;
            this.totalViews = totalViews;
            this.totalInLists = totalInLists;
        }

        public int getTotalUsers() { return totalUsers; }
        public int getActiveUsers() { return activeUsers; }
        public int getTotalMovies() { return totalMovies; }
        public int getTotalSeries() { return totalSeries; }
        public int getTotalContent() { return totalMovies + totalSeries; }
        public int getTotalRatings() { return totalRatings; }
        public int getTotalComments() { return totalComments; }
        public int getTotalViews() { return totalViews; }
        public int getTotalInLists() { return totalInLists; }

        public double getAverageRatingsPerUser() {
            return totalUsers == 0 ? 0 : (double) totalRatings / totalUsers;
        }

        public double getAverageCommentsPerUser() {
            return totalUsers == 0 ? 0 : (double) totalComments / totalUsers;
        }
    }

    /**
     * Classe pour les statistiques d'engagement
     */
    public static class EngagementStatistics {
        private final double averageRatingsPerUser;
        private final double averageCommentsPerUser;
        private final double averageWatchTimePerUser;
        private final List<ActiveUser> mostActiveUsers;

        public EngagementStatistics(double averageRatingsPerUser, double averageCommentsPerUser,
                                    double averageWatchTimePerUser, List<ActiveUser> mostActiveUsers) {
            this.averageRatingsPerUser = averageRatingsPerUser;
            this.averageCommentsPerUser = averageCommentsPerUser;
            this.averageWatchTimePerUser = averageWatchTimePerUser;
            this.mostActiveUsers = mostActiveUsers;
        }

        public double getAverageRatingsPerUser() { return averageRatingsPerUser; }
        public double getAverageCommentsPerUser() { return averageCommentsPerUser; }
        public double getAverageWatchTimePerUser() { return averageWatchTimePerUser; }
        public List<ActiveUser> getMostActiveUsers() { return mostActiveUsers; }
    }

    /**
     * Classe pour les utilisateurs actifs
     */
    public static class ActiveUser {
        private final int userId;
        private final String username;
        private final long activityCount;

        public ActiveUser(int userId, String username, long activityCount) {
            this.userId = userId;
            this.username = username;
            this.activityCount = activityCount;
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public long getActivityCount() { return activityCount; }
    }

    /**
     * Classe pour les statistiques par média
     */
    public static class MediaStatistics {
        private final int mediaId;
        private final String title;
        private final String type;
        private final int totalRatings;
        private final double averageRating;
        private final Map<Integer, Integer> ratingDistribution;
        private final int totalComments;
        private final int totalViews;
        private final int totalInLists;

        public MediaStatistics(int mediaId, String title, String type, int totalRatings,
                               double averageRating, Map<Integer, Integer> ratingDistribution,
                               int totalComments, int totalViews, int totalInLists) {
            this.mediaId = mediaId;
            this.title = title;
            this.type = type;
            this.totalRatings = totalRatings;
            this.averageRating = averageRating;
            this.ratingDistribution = ratingDistribution;
            this.totalComments = totalComments;
            this.totalViews = totalViews;
            this.totalInLists = totalInLists;
        }

        public int getMediaId() { return mediaId; }
        public String getTitle() { return title; }
        public String getType() { return type; }
        public int getTotalRatings() { return totalRatings; }
        public double getAverageRating() { return averageRating; }
        public Map<Integer, Integer> getRatingDistribution() { return ratingDistribution; }
        public int getTotalComments() { return totalComments; }
        public int getTotalViews() { return totalViews; }
        public int getTotalInLists() { return totalInLists; }

        public String getStarRating() {
            int fullStars = (int) Math.round(averageRating);
            return "★".repeat(fullStars) + "☆".repeat(5 - fullStars);
        }

        public int getFiveStarCount() { return ratingDistribution.getOrDefault(5, 0); }
        public int getFourStarCount() { return ratingDistribution.getOrDefault(4, 0); }
        public int getThreeStarCount() { return ratingDistribution.getOrDefault(3, 0); }
        public int getTwoStarCount() { return ratingDistribution.getOrDefault(2, 0); }
        public int getOneStarCount() { return ratingDistribution.getOrDefault(1, 0); }
    }
}