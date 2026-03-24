package org.netflix.DAO;

import org.netflix.Models.Genre;
import org.netflix.Models.Movie;
import org.netflix.Utils.ConxDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {
    private static Connection conn = ConxDB.getInstance();

    public static List<Movie> getAllMovies(){
        List<Movie> movies = new ArrayList<Movie>();
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
                            rs.getString("director"),
                            rs.getString("videoUrl"),
                            rs.getInt("duration_minutes"),
                            genresList
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
                        rs.getString("director"),
                        rs.getString("videoUrl"),
                        rs.getInt("duration_minutes"),
                        genreList
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
}
