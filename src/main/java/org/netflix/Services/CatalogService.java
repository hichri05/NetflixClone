// package org.netflix.Services;

        package org.netflix.Services;

import org.netflix.DAO.*;
import org.netflix.Models.*;

import java.util.*;
import java.util.stream.Collectors;

public class CatalogService {

    private final MovieDAO movieDAO;
    private final SerieDAO serieDAO;
    private final GenreDAO genreDAO;
    private final MediaGenreDAO mediaGenreDAO;
    private final RatingDAO ratingDAO;

    public CatalogService() {
        this.movieDAO = new MovieDAO();
        this.serieDAO = new SerieDAO();
        this.genreDAO = new GenreDAO();
        this.mediaGenreDAO = new MediaGenreDAO();
        this.ratingDAO = new RatingDAO();
    }

    // ==================== MÉTHODES DE RÉCUPÉRATION ====================

    /**
     * Récupère tous les médias (films et séries) actifs
     * @return Liste de tous les médias
     */
    public List<Media> getAllMedia() {
        List<Media> allMedia = new ArrayList<>();

        // Récupérer tous les films
        List<Movie> movies = movieDAO.findAll();
        allMedia.addAll(movies);

        // Récupérer toutes les séries
        List<Serie> series = serieDAO.findAll();
        allMedia.addAll(series);

        // Trier par année de sortie (plus récent d'abord)
        allMedia.sort((m1, m2) -> Integer.compare(m2.getReleaseYear(), m1.getReleaseYear()));

        return allMedia;
    }

    /**
     * Récupère les médias "À la une" (top 10 des mieux notés)
     * @return Liste des médias à la une
     */
    public List<Media> getFeaturedMedia() {
        List<Media> allMedia = getAllMedia();

        // Règle métier : Top 10 des médias avec la meilleure note moyenne
        return allMedia.stream()
                .sorted((m1, m2) -> Double.compare(m2.getAverageRating(), m1.getAverageRating()))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les films par genre
     * @param genreName Nom du genre
     * @return Liste des films du genre
     */
    public List<Movie> getMoviesByGenre(String genreName) {
        Optional<Genre> genreOpt = genreDAO.findByName(genreName);

        if (genreOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Genre genre = genreOpt.get();
        return movieDAO.findByGenre(genre.getId());
    }

    /**
     * Récupère les séries par genre
     * @param genreName Nom du genre
     * @return Liste des séries du genre
     */
    public List<Serie> getSeriesByGenre(String genreName) {
        Optional<Genre> genreOpt = genreDAO.findByName(genreName);

        if (genreOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Genre genre = genreOpt.get();
        return serieDAO.findByGenre(genre.getId());
    }

    /**
     * Récupère un média par son ID et son type
     * @param mediaId ID du média
     * @param type "MOVIE" ou "SERIE"
     * @return Optional contenant le média
     */
    public Optional<Media> getMediaById(int mediaId, String type) {
        if ("MOVIE".equalsIgnoreCase(type)) {
            Optional<Movie> movieOpt = movieDAO.findById(mediaId);
            if (movieOpt.isPresent()) {
                // Charger les genres du film
                List<Genre> genres = mediaGenreDAO.findGenresByMedia(mediaId, "MOVIE");
                movieOpt.get().getGenres().addAll(genres);
                return movieOpt.map(m -> (Media) m);
            }
        } else if ("SERIE".equalsIgnoreCase(type)) {
            Optional<Serie> serieOpt = serieDAO.findById(mediaId);
            if (serieOpt.isPresent()) {
                // Charger les genres de la série
                List<Genre> genres = mediaGenreDAO.findGenresByMedia(mediaId, "SERIE");
                serieOpt.get().getGenres().addAll(genres);
                return serieOpt.map(s -> (Media) s);
            }
        }
        return Optional.empty();
    }

    /**
     * Récupère un film par son ID
     * @param movieId ID du film
     * @return Optional contenant le film
     */
    public Optional<Movie> getMovieById(int movieId) {
        Optional<Movie> movieOpt = movieDAO.findById(movieId);
        if (movieOpt.isPresent()) {
            Movie movie = movieOpt.get();
            // Charger les genres du film
            List<Genre> genres = mediaGenreDAO.findGenresByMedia(movieId, "MOVIE");
            movie.getGenres().addAll(genres);
            // Charger la note moyenne
            double avgRating = ratingDAO.calculateAverageRating(movieId);
            movie.setAverageRating(avgRating);
            return Optional.of(movie);
        }
        return Optional.empty();
    }

    /**
     * Récupère une série par son ID
     * @param serieId ID de la série
     * @return Optional contenant la série
     */
    public Optional<Serie> getSerieById(int serieId) {
        Optional<Serie> serieOpt = serieDAO.findById(serieId);
        if (serieOpt.isPresent()) {
            Serie serie = serieOpt.get();
            // Charger les genres de la série
            List<Genre> genres = mediaGenreDAO.findGenresByMedia(serieId, "SERIE");
            serie.getGenres().addAll(genres);
            // Charger la note moyenne
            double avgRating = ratingDAO.calculateAverageRating(serieId);
            serie.setAverageRating(avgRating);
            return Optional.of(serie);
        }
        return Optional.empty();
    }

    public List<Media> searchMedia(String query, String genre, Integer year) {
        List<Media> results = new ArrayList<>();

        List<Movie> movies = movieDAO.search(query);
        List<Serie> series = serieDAO.search(query);
        results.addAll(movies);
        results.addAll(series);


        if (genre != null && !genre.trim().isEmpty()) {
            Optional<Genre> genreOpt = genreDAO.findByName(genre);
            if (genreOpt.isPresent()) {
                int genreId = genreOpt.get().getId();
                results = results.stream()
                        .filter(media -> {
                            List<Genre> mediaGenres = getGenresForMedia(media.getIdMedia(), media.getType());
                            return mediaGenres.stream().anyMatch(g -> g.getId() == genreId);
                        })
                        .collect(Collectors.toList());
            }
        }

        if (year != null) {
            results = results.stream()
                    .filter(media -> media.getReleaseYear() == year)
                    .collect(Collectors.toList());
        }

        results.sort((m1, m2) -> Double.compare(m2.getAverageRating(), m1.getAverageRating()));

        return results;
    }

    public List<Media> searchByTitle(String title) {
        List<Media> results = new ArrayList<>();

        List<Movie> movies = movieDAO.search(title);
        List<Serie> series = serieDAO.search(title);
        results.addAll(movies);
        results.addAll(series);

        return results;
    }

    /**
     * Récupère les médias d'une année spécifique
     * @param year Année de sortie
     * @return Liste des médias sortis cette année
     */
    public List<Media> getMediaByYear(int year) {
        List<Media> allMedia = getAllMedia();

        return allMedia.stream()
                .filter(media -> media.getReleaseYear() == year)
                .collect(Collectors.toList());
    }

    // ==================== MÉTHODES DE NAVIGATION PAR CARROUSELS ====================

    /**
     * Récupère les médias pour un carrousel par genre
     * @param genreName Nom du genre
     * @param limit Nombre maximum de médias
     * @return Liste des médias du genre
     */
    public List<Media> getCarouselByGenre(String genreName, int limit) {
        List<Media> medias = new ArrayList<>();

        Optional<Genre> genreOpt = genreDAO.findByName(genreName);
        if (genreOpt.isPresent()) {
            int genreId = genreOpt.get().getId();

            List<Movie> movies = movieDAO.findByGenre(genreId);
            List<Serie> series = serieDAO.findByGenre(genreId);

            medias.addAll(movies);
            medias.addAll(series);

            // Limiter le nombre et trier par note
            medias = medias.stream()
                    .sorted((m1, m2) -> Double.compare(m2.getAverageRating(), m1.getAverageRating()))
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        return medias;
    }

    /**
     * Récupère les dernières sorties
     * @param limit Nombre maximum de médias
     * @return Liste des dernières sorties
     */
    public List<Media> getLatestReleases(int limit) {
        List<Media> allMedia = getAllMedia();

        return allMedia.stream()
                .sorted((m1, m2) -> Integer.compare(m2.getReleaseYear(), m1.getReleaseYear()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les médias les mieux notés
     * @param limit Nombre maximum de médias
     * @return Liste des mieux notés
     */
    public List<Media> getTopRatedMedia(int limit) {
        List<Media> allMedia = getAllMedia();

        return allMedia.stream()
                .sorted((m1, m2) -> Double.compare(m2.getAverageRating(), m1.getAverageRating()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ==================== MÉTHODES POUR LES GENRES ====================

    /**
     * Récupère tous les genres disponibles
     * @return Liste de tous les genres
     */
    public List<Genre> getAllGenres() {
        return genreDAO.findAll();
    }

    /**
     * Récupère les genres d'un média spécifique
     * @param mediaId ID du média
     * @param mediaType Type du média ("MOVIE" ou "SERIE")
     * @return Liste des genres du média
     */
    public List<Genre> getGenresForMedia(int mediaId, String mediaType) {
        return mediaGenreDAO.findGenresByMedia(mediaId, mediaType);
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Vérifie si un média existe
     * @param mediaId ID du média
     * @param type Type du média
     * @return true si existe, false sinon
     */
    public boolean mediaExists(int mediaId, String type) {
        if ("MOVIE".equalsIgnoreCase(type)) {
            return movieDAO.findById(mediaId).isPresent();
        } else if ("SERIE".equalsIgnoreCase(type)) {
            return serieDAO.findById(mediaId).isPresent();
        }
        return false;
    }

    /**
     * Récupère le nombre total de films
     * @return Nombre de films
     */
    public int getTotalMoviesCount() {
        return movieDAO.count();
    }

    /**
     * Récupère le nombre total de séries
     * @return Nombre de séries
     */
    public int getTotalSeriesCount() {
        return serieDAO.count();
    }

    /**
     * Récupère les films par réalisateur
     * @param director Nom du réalisateur
     * @return Liste des films du réalisateur
     */
    public List<Movie> getMoviesByDirector(String director) {
        return movieDAO.findByDirector(director);
    }
}