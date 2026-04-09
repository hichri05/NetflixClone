package org.netflix.Services;

import org.netflix.DAO.MediaDAO;
import org.netflix.DAO.MovieDAO;
import org.netflix.DAO.SerieDAO;
import org.netflix.Models.Media;
import org.netflix.Models.Movie;
import org.netflix.Models.Serie;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CatalogService {


    public Movie getTrendingMedia() {
        Movie trend = MovieDAO.getTrendMovie();
        if (trend == null) {
            System.err.println("Aucun film tendance trouvé.");
        }
        return trend;
    }

    public List<Media> getTopTenMostWatched() {
        return MediaDAO.getTopViews();
    }


    public List<Media> getMediasByGenre(String genreName) {
        List<Media> allMedias = new ArrayList<>();


        allMedias.addAll(MovieDAO.findbyGenre(genreName));


        allMedias.addAll(SerieDAO.findbyGenre(genreName));

        return allMedias;
    }


    public List<Media> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return MediaDAO.searchMedia(query);
    }


    public List<Serie> getAllSeriesByGenre(String genreName) {
        return SerieDAO.findbyGenre(genreName);
    }
}