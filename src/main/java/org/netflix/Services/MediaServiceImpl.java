package org.netflix.Services;

import org.netflix.DAO.MediaDAO;
import org.netflix.DAO.MovieDAO;
import org.netflix.DAO.RatingDAO;
import org.netflix.DAO.SerieDAO;
import org.netflix.Models.Media;
import org.netflix.Models.Movie;
import org.netflix.Models.Serie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MediaServiceImpl implements IMediaService {

    private final MediaDAO mediaDAO;
    private final RatingDAO ratingDAO;

    public MediaServiceImpl(MediaDAO mediaDAO, RatingDAO ratingDAO) {
        this.mediaDAO = mediaDAO;
        this.ratingDAO = ratingDAO;
    }

    @Override
    public List<Media> getAllMedia() {
        return MediaDAO.getAllMedia();
    }

    @Override
    public List<Movie> getAllMovies() {
        return MovieDAO.getAllMovies();
    }

    @Override
    public List<Serie> getAllSeries() {
        return new SerieDAO().getAllSeries();
    }

    @Override
    public Media getMediaById(int id) {
        // Try movie first, fall back to searching all media
        Movie movie = MovieDAO.getMovieById(id);
        if (movie != null) return movie;

        // Search in all media list
        return MediaDAO.getAllMedia().stream()
                .filter(m -> m.getIdMedia() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Media> searchMedia(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllMedia();
        return MediaDAO.searchMedia(keyword.trim());
    }

    @Override
    public List<Media> filterByGenre(String genre) {
        return MediaDAO.getMediasByGenre(genre);
    }

    @Override
    public List<Media> filterByYear(int year) {
        // Filter from all media in memory — no dedicated DAO method needed
        return MediaDAO.getAllMedia().stream()
                .filter(m -> m.getReleaseYear() == year)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Media> getFeaturedMedia() {
        // Top 5 by average rating
        return MediaDAO.getAllMediaWithViews().stream()
                .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Media> getTop5MostWatched() {
        return MediaDAO.getTopViews().stream()
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Map<String, Long> getMediaCountByGenre() {
        Map<String, Long> genreCountMap = new HashMap<>();
        List<Media> allMedia = MediaDAO.getAllMediaWithViews();

        for (Media media : allMedia) {
            if (media.getGenres() == null) continue;
            media.getGenres().forEach(g -> {
                if (g != null && g.getName() != null) {
                    String name = g.toString();
                    genreCountMap.put(name, genreCountMap.getOrDefault(name, 0L) + 1);
                }
            });
        }
        return genreCountMap;
    }

    @Override
    public boolean addMovie(Movie movie) {
        if (movie == null || movie.getTitle() == null || movie.getTitle().isBlank())
            return false;
        return MediaDAO.addMedia(movie);
    }

    @Override
    public boolean addSerie(Serie serie) {
        if (serie == null || serie.getTitle() == null || serie.getTitle().isBlank())
            return false;
        return MediaDAO.addMedia(serie);
    }

    @Override
    public boolean updateMedia(Media media) {
        if (media == null) return false;
        return MediaDAO.updateMedia(media);
    }

    @Override
    public boolean deleteMedia(int id) {
        return MediaDAO.deleteMedia(id);
    }

    @Override
    public double calculateAverageRating(int idMedia) {
        List<org.netflix.Models.Rating> ratings = RatingDAO.findByMedia(idMedia);
        if (ratings == null || ratings.isEmpty()) return 0.0;

        double avg = ratings.stream()
                .mapToDouble(r -> r.getScore())
                .average()
                .orElse(0.0);

        // Persist the recalculated average back to the media row
        MediaDAO.updateMedia(
                MediaDAO.getAllMedia().stream()
                        .filter(m -> m.getIdMedia() == idMedia)
                        .findFirst()
                        .map(m -> { m.setAverageRating(avg); return m; })
                        .orElse(null)
        );
        return avg;
    }
}