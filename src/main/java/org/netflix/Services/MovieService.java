package org.netflix.Services;

import org.netflix.DAO.MovieDAO;
import org.netflix.Models.Movie;

import java.util.List;

public class MovieService {
    public static List<Movie> getMoviesByGenre(String action) {
        return MovieDAO.findbyGenre("Action");
    }


}
