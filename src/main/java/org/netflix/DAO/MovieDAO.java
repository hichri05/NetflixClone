package org.netflix.DAO;

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
                    movies.add(new Movie(
                            rs.getInt("id_Media"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("releaseYear"),
                            rs.getDouble("averageRating"),
                            rs.getString("coverImageUrl"),
                            rs.getString("director"),
                            rs.getString("videoUrl"),
                            rs.getInt("duration_minutes")
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
}
