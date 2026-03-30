package org.netflix.DAO;

import org.netflix.Models.Acteur;
import org.netflix.Models.Genre;
import org.netflix.Models.Movie;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {
    private static Connection conn = ConxDB.getInstance();

    public static List<Movie> getAllMovies(){
        List<Movie> movies = new ArrayList<Movie>();
        List<Acteur> casting=new ArrayList<Acteur>();
        String sql = "SELECT m.*, v.videoUrl, v.duration_minutes " +
                "FROM media m " +
                "INNER JOIN movie v ON m.id_Media = v.id_Media";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            try {
                while (rs.next()) {
                    List<Genre> genresList = MediaDAO.getGenresByMediaId(rs.getInt("id_Media"));

                    movies.add(new Movie(
                            rs.getInt("id_Media"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("releaseYear"),
                            rs.getDouble("averageRating"),
                            rs.getString("coverImageUrl"),
                            rs.getString("backdrop_path"),
                            rs.getString("director"),
                            rs.getString("videoUrl"),
                            rs.getInt("duration_minutes"),
                            genresList,
                            casting
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  movies;
    }
    public static Movie getTrendMovie() {
        Movie movie = null;
        List<Acteur> casting=new ArrayList<Acteur>();
        String sql = "SELECT m.*, v.videoUrl, v.duration_minutes " +
                "FROM Media m " +
                "INNER JOIN Movie v ON m.id_Media = v.id_Media " +
                "ORDER BY m.averageRating DESC LIMIT 1" ;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int currentId = rs.getInt("id_Media");

                List<Genre> genreList = MediaDAO.getGenresByMediaId(currentId);


                movie = new Movie(
                        currentId,
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("releaseYear"),
                        rs.getDouble("averageRating"),
                        rs.getString("coverImageUrl"),
                        rs.getString("backdrop_path"),
                        rs.getString("director"),
                        rs.getString("videoUrl"),
                        rs.getInt("duration_minutes"),
                        genreList,
                        casting
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movie;
    }

    public static List<Movie> findbyGenre(String genre) {
        //todo
        return null;
    }

    public boolean updateRating(Movie movie) {
        String sql = "UPDATE movies SET average_rating = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, movie.getAverageRating());
            pstmt.setInt(2, movie.getIdMedia());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

        public List<Movie> findTopRated(int limit) {
            String sql = "SELECT * FROM movies ORDER BY average_rating DESC LIMIT ?";
            List<Movie> movies = new ArrayList<>();

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, limit);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        //movies.add(ResultSetToMovie(rs));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return movies;
        }
}