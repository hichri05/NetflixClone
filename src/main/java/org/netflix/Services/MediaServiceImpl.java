package org.netflix.Services;

import org.netflix.DAO.MediaDAO;
import org.netflix.DAO.RatingDAO;
import org.netflix.Models.*;
import java.util.*;

public class MediaServiceImpl implements IMediaService {

    private final MediaDAO mediaDAO;
    private final RatingDAO ratingDAO;

    public MediaServiceImpl(MediaDAO mediaDAO, RatingDAO ratingDAO) {
        this.mediaDAO = mediaDAO;
        this.ratingDAO = ratingDAO;
    }

    @Override
    public List<Media> getAllMedia() {
        return mediaDAO.findAll();
    }

    @Override
    public List<Movie> getAllMovies() {
        return mediaDAO.findAllMovies();
    }

    @Override
    public List<Serie> getAllSeries() {
        return mediaDAO.findAllSeries();
    }

    @Override
    public Media getMediaById(int id) {
        return mediaDAO.findById(id);
    }

    @Override
    public List<Media> searchMedia(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllMedia();
        return mediaDAO.searchByKeyword(keyword.trim());
    }

    @Override
    public List<Media> filterByGenre(String genre) {
        return mediaDAO.findByGenre(genre);
    }

    @Override
    public List<Media> filterByYear(int year) {
        return mediaDAO.findByYear(year);
    }

    @Override
    public List<Media> getFeaturedMedia() {
        // Les 5 médias les plus récents avec le meilleur rating
        return mediaDAO.findFeatured();
    }

    @Override
    public List<Media> getTop5MostWatched() {
        return mediaDAO.findTop5ByViews();
    }

    @Override
    public Map<String, Long> getMediaCountByGenre() {
        return mediaDAO.countByGenre();
    }

    @Override
    public boolean addMovie(Movie movie) {
        if (movie == null || movie.getTitle() == null || movie.getTitle().isBlank())
            return false;
        return mediaDAO.insertMovie(movie);
    }

    @Override
    public boolean addSerie(Serie serie) {
        if (serie == null || serie.getTitle() == null || serie.getTitle().isBlank())
            return false;
        return mediaDAO.insertSerie(serie);
    }

    @Override
    public boolean updateMedia(Media media) {
        if (media == null) return false;
        return mediaDAO.update(media);
    }

    @Override
    public boolean deleteMedia(int id) {
        return mediaDAO.delete(id);
    }

    @Override
    public double calculateAverageRating(int idMedia) {
        List<Rating> ratings = ratingDAO.findByMedia(idMedia);
        if (ratings == null || ratings.isEmpty()) return 0.0;
        double sum = 0;
        for (Rating r : ratings) sum += r.getScore();
        double avg = sum / ratings.size();
        // Mise à jour en BDD
        mediaDAO.updateAverageRating(idMedia, avg);
        return avg;
    }
}