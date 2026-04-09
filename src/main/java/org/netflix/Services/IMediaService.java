package org.netflix.Services;

import org.netflix.Models.Media;
import org.netflix.Models.Movie;
import org.netflix.Models.Serie;
import java.util.List;
import java.util.Map;

public interface IMediaService {
    List<Media> getAllMedia();
    List<Movie> getAllMovies();
    List<Serie> getAllSeries();
    Media getMediaById(int id);
    List<Media> searchMedia(String keyword);
    List<Media> filterByGenre(String genre);
    List<Media> filterByYear(int year);
    List<Media> getFeaturedMedia();           // "À la une"
    List<Media> getTop5MostWatched();         // Dashboard
    Map<String, Long> getMediaCountByGenre(); // Dashboard Pie Chart
    boolean addMovie(Movie movie);
    boolean addSerie(Serie serie);
    boolean updateMedia(Media media);
    boolean deleteMedia(int id);
    double calculateAverageRating(int idMedia);
}