// package org.netflix.Services;

package org.netflix.Services;

import org.netflix.DAO.MyListDAO;
import org.netflix.DAO.MovieDAO;
import org.netflix.DAO.SerieDAO;
import org.netflix.Models.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class UserListService {

    private final MyListDAO myListDAO;
    private final MovieDAO movieDAO;
    private final SerieDAO serieDAO;

    public UserListService() {
        this.myListDAO = new MyListDAO();
        this.movieDAO = new MovieDAO();
        this.serieDAO = new SerieDAO();
    }

    // ==================== AJOUT ET RETRAIT DE LA LISTE ====================

    /**
     * Ajoute un film à la liste de favoris d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param movieId ID du film
     * @return true si ajout réussi, false sinon
     */
    public boolean addMovieToList(int userId, int movieId) {
        // 1. Vérifier que le film existe
        Optional<Movie> movieOpt = movieDAO.findById(movieId);
        if (movieOpt.isEmpty()) {
            System.err.println("Film non trouvé: " + movieId);
            return false;
        }

        // 2. Vérifier que le film n'est pas déjà dans la liste
        if (myListDAO.isMovieInList(userId, movieId)) {
            System.err.println("Film déjà dans la liste de l'utilisateur");
            return false;
        }

        // 3. Ajouter à la liste
        MyList myListItem = new MyList(
                userId,
                movieId,
                null,
                Timestamp.valueOf(LocalDateTime.now())
        );

        boolean added = myListDAO.insert(myListItem);

        if (added) {
            System.out.println("Film ajouté à la liste de l'utilisateur " + userId);
        }

        return added;
    }

    /**
     * Ajoute une série à la liste de favoris d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param serieId ID de la série
     * @return true si ajout réussi, false sinon
     */
    public boolean addSerieToList(int userId, int serieId) {
        // 1. Vérifier que la série existe
        Optional<Serie> serieOpt = serieDAO.findById(serieId);
        if (serieOpt.isEmpty()) {
            System.err.println("Série non trouvée: " + serieId);
            return false;
        }

        // 2. Vérifier que la série n'est pas déjà dans la liste
        if (myListDAO.isSerieInList(userId, serieId)) {
            System.err.println("Série déjà dans la liste de l'utilisateur");
            return false;
        }

        // 3. Ajouter à la liste
        MyList myListItem = new MyList(
                userId,
                null,
                serieId,
                Timestamp.valueOf(LocalDateTime.now())
        );

        boolean added = myListDAO.insert(myListItem);

        if (added) {
            System.out.println("Série ajoutée à la liste de l'utilisateur " + userId);
        }

        return added;
    }

    /**
     * Ajoute un média (film ou série) à la liste
     * @param userId ID de l'utilisateur
     * @param mediaId ID du média
     * @param type Type du média ("MOVIE" ou "SERIE")
     * @return true si ajout réussi
     */
    public boolean addToList(int userId, int mediaId, String type) {
        if ("MOVIE".equalsIgnoreCase(type)) {
            return addMovieToList(userId, mediaId);
        } else if ("SERIE".equalsIgnoreCase(type)) {
            return addSerieToList(userId, mediaId);
        }
        return false;
    }

    /**
     * Retire un film de la liste de favoris d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param movieId ID du film
     * @return true si retrait réussi
     */
    public boolean removeMovieFromList(int userId, int movieId) {
        boolean removed = myListDAO.deleteMovie(userId, movieId);

        if (removed) {
            System.out.println("Film retiré de la liste de l'utilisateur " + userId);
        }

        return removed;
    }

    /**
     * Retire une série de la liste de favoris d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param serieId ID de la série
     * @return true si retrait réussi
     */
    public boolean removeSerieFromList(int userId, int serieId) {
        boolean removed = myListDAO.deleteSerie(userId, serieId);

        if (removed) {
            System.out.println("Série retirée de la liste de l'utilisateur " + userId);
        }

        return removed;
    }

    /**
     * Retire un média de la liste
     * @param userId ID de l'utilisateur
     * @param mediaId ID du média
     * @param type Type du média
     * @return true si retrait réussi
     */
    public boolean removeFromList(int userId, int mediaId, String type) {
        if ("MOVIE".equalsIgnoreCase(type)) {
            return removeMovieFromList(userId, mediaId);
        } else if ("SERIE".equalsIgnoreCase(type)) {
            return removeSerieFromList(userId, mediaId);
        }
        return false;
    }

    // ==================== RÉCUPÉRATION DE LA LISTE ====================

    /**
     * Récupère tous les films dans la liste de l'utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des films favoris
     */
    public List<Movie> getUserMoviesList(int userId) {
        List<Integer> movieIds = myListDAO.findMovieIdsByUser(userId);
        List<Movie> movies = new ArrayList<>();

        for (int movieId : movieIds) {
            Optional<Movie> movieOpt = movieDAO.findById(movieId);
            movieOpt.ifPresent(movies::add);
        }

        // Trier par date d'ajout (plus récent d'abord)
        movies.sort((m1, m2) -> {
            Optional<Timestamp> date1 = myListDAO.getAddedAt(userId, m1.getIdMedia(), "MOVIE");
            Optional<Timestamp> date2 = myListDAO.getAddedAt(userId, m2.getIdMedia(), "MOVIE");

            if (date1.isPresent() && date2.isPresent()) {
                return date2.get().compareTo(date1.get());
            }
            return 0;
        });

        return movies;
    }

    /**
     * Récupère toutes les séries dans la liste de l'utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des séries favorites
     */
    public List<Serie> getUserSeriesList(int userId) {
        List<Integer> serieIds = myListDAO.findSerieIdsByUser(userId);
        List<Serie> series = new ArrayList<>();

        for (int serieId : serieIds) {
            Optional<Serie> serieOpt = serieDAO.findById(serieId);
            serieOpt.ifPresent(series::add);
        }

        // Trier par date d'ajout (plus récent d'abord)
        series.sort((s1, s2) -> {
            Optional<Timestamp> date1 = myListDAO.getAddedAt(userId, s1.getIdMedia(), "SERIE");
            Optional<Timestamp> date2 = myListDAO.getAddedAt(userId, s2.getIdMedia(), "SERIE");

            if (date1.isPresent() && date2.isPresent()) {
                return date2.get().compareTo(date1.get());
            }
            return 0;
        });

        return series;
    }

    /**
     * Récupère tous les médias (films et séries) dans la liste de l'utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des médias favoris avec leur type
     */
    public List<ListItem> getUserFullList(int userId) {
        List<ListItem> items = new ArrayList<>();

        // Ajouter les films
        List<Movie> movies = getUserMoviesList(userId);
        for (Movie movie : movies) {
            Optional<Timestamp> addedAt = myListDAO.getAddedAt(userId, movie.getIdMedia(), "MOVIE");
            items.add(new ListItem(
                    movie.getIdMedia(),
                    movie.getTitle(),
                    movie.getCoverImageUrl(),
                    "MOVIE",
                    addedAt.orElse(null),
                    movie.getAverageRating()
            ));
        }

        // Ajouter les séries
        List<Serie> series = getUserSeriesList(userId);
        for (Serie serie : series) {
            Optional<Timestamp> addedAt = myListDAO.getAddedAt(userId, serie.getIdMedia(), "SERIE");
            items.add(new ListItem(
                    serie.getIdMedia(),
                    serie.getTitle(),
                    serie.getCoverImageUrl(),
                    "SERIE",
                    addedAt.orElse(null),
                    serie.getAverageRating()
            ));
        }

        // Trier par date d'ajout (plus récent d'abord)
        items.sort((i1, i2) -> {
            if (i1.getAddedAt() != null && i2.getAddedAt() != null) {
                return i2.getAddedAt().compareTo(i1.getAddedAt());
            }
            return 0;
        });

        return items;
    }

    /**
     * Récupère les derniers médias ajoutés à la liste
     * @param userId ID de l'utilisateur
     * @param limit Nombre maximum de résultats
     * @return Liste des derniers médias ajoutés
     */
    public List<ListItem> getRecentAdded(int userId, int limit) {
        List<ListItem> fullList = getUserFullList(userId);

        if (fullList.size() > limit) {
            return fullList.subList(0, limit);
        }
        return fullList;
    }

    // ==================== VÉRIFICATIONS ====================

    /**
     * Vérifie si un film est dans la liste de l'utilisateur
     * @param userId ID de l'utilisateur
     * @param movieId ID du film
     * @return true si dans la liste
     */
    public boolean isMovieInList(int userId, int movieId) {
        return myListDAO.isMovieInList(userId, movieId);
    }

    /**
     * Vérifie si une série est dans la liste de l'utilisateur
     * @param userId ID de l'utilisateur
     * @param serieId ID de la série
     * @return true si dans la liste
     */
    public boolean isSerieInList(int userId, int serieId) {
        return myListDAO.isSerieInList(userId, serieId);
    }

    /**
     * Vérifie si un média est dans la liste de l'utilisateur
     * @param userId ID de l'utilisateur
     * @param mediaId ID du média
     * @param type Type du média
     * @return true si dans la liste
     */
    public boolean isInList(int userId, int mediaId, String type) {
        if ("MOVIE".equalsIgnoreCase(type)) {
            return isMovieInList(userId, mediaId);
        } else if ("SERIE".equalsIgnoreCase(type)) {
            return isSerieInList(userId, mediaId);
        }
        return false;
    }

    // ==================== STATISTIQUES ====================

    /**
     * Compte le nombre de films dans la liste de l'utilisateur
     * @param userId ID de l'utilisateur
     * @return Nombre de films
     */
    public int getMovieCountInList(int userId) {
        return myListDAO.countMoviesByUser(userId);
    }

    /**
     * Compte le nombre de séries dans la liste de l'utilisateur
     * @param userId ID de l'utilisateur
     * @return Nombre de séries
     */
    public int getSerieCountInList(int userId) {
        return myListDAO.countSeriesByUser(userId);
    }

    /**
     * Compte le nombre total de médias dans la liste de l'utilisateur
     * @param userId ID de l'utilisateur
     * @return Nombre total
     */
    public int getTotalCountInList(int userId) {
        return getMovieCountInList(userId) + getSerieCountInList(userId);
    }

    // ==================== RECOMMANDATIONS ====================

    /**
     * Recommande des films basés sur la liste de l'utilisateur
     * @param userId ID de l'utilisateur
     * @param limit Nombre maximum de recommandations
     * @return Liste des films recommandés
     */
    public List<Movie> recommendMoviesBasedOnList(int userId, int limit) {
        List<Movie> userMovies = getUserMoviesList(userId);

        if (userMovies.isEmpty()) {
            // Si la liste est vide, recommander les films les mieux notés
            return movieDAO.findTopRated(limit);
        }

        // Récupérer les genres des films dans la liste de l'utilisateur
        Set<Integer> preferredGenres = extractGenresFromMovies(userMovies);

        // Recommander des films avec des genres similaires
        List<Movie> recommendations = new ArrayList<>();

        for (int genreId : preferredGenres) {
            List<Movie> moviesByGenre = movieDAO.findByGenre(genreId);
            for (Movie movie : moviesByGenre) {
                // Ne pas recommander les films déjà dans la liste
                if (!isMovieInList(userId, movie.getIdMedia())) {
                    recommendations.add(movie);
                }
            }
        }

        // Supprimer les doublons
        recommendations = recommendations.stream()
                .distinct()
                .collect(Collectors.toList());

        // Trier par note moyenne
        recommendations.sort((m1, m2) -> Double.compare(m2.getAverageRating(), m1.getAverageRating()));

        if (recommendations.size() > limit) {
            return recommendations.subList(0, limit);
        }
        return recommendations;
    }

    /**
     * Recommande des séries basées sur la liste de l'utilisateur
     * @param userId ID de l'utilisateur
     * @param limit Nombre maximum de recommandations
     * @return Liste des séries recommandées
     */
    public List<Serie> recommendSeriesBasedOnList(int userId, int limit) {
        List<Serie> userSeries = getUserSeriesList(userId);

        if (userSeries.isEmpty()) {
            // Si la liste est vide, recommander les séries les mieux notées
            return serieDAO.findTopRated(limit);
        }

        // Récupérer les genres des séries dans la liste de l'utilisateur
        Set<Integer> preferredGenres = extractGenresFromSeries(userSeries);

        // Recommander des séries avec des genres similaires
        List<Serie> recommendations = new ArrayList<>();

        for (int genreId : preferredGenres) {
            List<Serie> seriesByGenre = serieDAO.findByGenre(genreId);
            for (Serie serie : seriesByGenre) {
                if (!isSerieInList(userId, serie.getIdMedia())) {
                    recommendations.add(serie);
                }
            }
        }

        // Supprimer les doublons
        recommendations = recommendations.stream()
                .distinct()
                .collect(Collectors.toList());

        // Trier par note moyenne
        recommendations.sort((s1, s2) -> Double.compare(s2.getAverageRating(), s1.getAverageRating()));

        if (recommendations.size() > limit) {
            return recommendations.subList(0, limit);
        }
        return recommendations;
    }

    /**
     * Extrait les genres des films (méthode utilitaire)
     */
    private Set<Integer> extractGenresFromMovies(List<Movie> movies) {
        Set<Integer> genres = new HashSet<>();
        // À implémenter selon votre structure de données
        // Pour l'instant, retourne un set vide
        return genres;
    }

    /**
     * Extrait les genres des séries (méthode utilitaire)
     */
    private Set<Integer> extractGenresFromSeries(List<Serie> series) {
        Set<Integer> genres = new HashSet<>();
        // À implémenter selon votre structure de données
        return genres;
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Classe pour représenter un élément de la liste (film ou série)
     */
    public static class ListItem {
        private final int id;
        private final String title;
        private final String coverImageUrl;
        private final String type;
        private final Timestamp addedAt;
        private final double averageRating;

        public ListItem(int id, String title, String coverImageUrl, String type,
                        Timestamp addedAt, double averageRating) {
            this.id = id;
            this.title = title;
            this.coverImageUrl = coverImageUrl;
            this.type = type;
            this.addedAt = addedAt;
            this.averageRating = averageRating;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getCoverImageUrl() { return coverImageUrl; }
        public String getType() { return type; }
        public Timestamp getAddedAt() { return addedAt; }
        public double getAverageRating() { return averageRating; }

        public String getFormattedRating() {
            return String.format("%.1f", averageRating);
        }

        public String getStarRating() {
            int fullStars = (int) Math.round(averageRating);
            return "★".repeat(fullStars) + "☆".repeat(5 - fullStars);
        }

        public String getTimeAgo() {
            if (addedAt == null) return "Date inconnue";

            long diff = System.currentTimeMillis() - addedAt.getTime();
            long minutes = diff / (60 * 1000);
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) return "Ajouté il y a " + days + " jour" + (days > 1 ? "s" : "");
            if (hours > 0) return "Ajouté il y a " + hours + " heure" + (hours > 1 ? "s" : "");
            if (minutes > 0) return "Ajouté il y a " + minutes + " minute" + (minutes > 1 ? "s" : "");
            return "Ajouté à l'instant";
        }

        @Override
        public String toString() {
            return title + " (" + type + ")";
        }
    }
}