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

    public static List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT m.*, v.videoUrl, v.duration_minutes " +
                "FROM media m " +
                "INNER JOIN movie v ON m.id_Media = v.id_Media";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int currentId = rs.getInt("id_Media");
                List<Genre> genresList = MediaDAO.getGenresByMediaId(currentId);
                List<Acteur> casting = new ArrayList<>();

                movies.add(new Movie(
                        currentId,
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("releaseYear"),
                        rs.getDouble("averageRating"),
                        rs.getString("coverImageUrl"),
                        rs.getString("coverImageUrl"),
                        rs.getString("director"),
                        "Movie",
                        genresList,
                        casting,
                        0,
                        rs.getString("videoUrl"),
                        rs.getInt("duration_minutes")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public static Movie getTrendMovie() {
        Movie movie = null;
        List<Acteur> casting = new ArrayList<>();
        String sql = "SELECT m.*, v.videoUrl, v.duration_minutes " +
                "FROM Media m " +
                "INNER JOIN Movie v ON m.id_Media = v.id_Media " +
                "ORDER BY m.averageRating DESC LIMIT 1";

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
                        rs.getString("type"),
                        genreList,
                        casting,
                        rs.getInt("views"),
                        rs.getString("videoUrl"),
                        rs.getInt("duration_minutes")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movie;
    }

    // ✅ SINGLE findbyGenre - fixed, no duplicate
    public static List<Movie> findbyGenre(String genreName) {
        List<Movie> movies = new ArrayList<>();

        String sql = "SELECT m.*, v.videoUrl, v.duration_minutes " +
                "FROM media m " +
                "INNER JOIN movie v ON m.id_Media = v.id_Media " +
                "INNER JOIN media_genres mg ON m.id_Media = mg.id_Media " +
                "INNER JOIN genres g ON mg.id_Genre = g.id_Genre " +
                "WHERE g.name = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, genreName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int currentId = rs.getInt("id_Media");
                    List<Genre> genreList = MediaDAO.getGenresByMediaId(currentId);
                    List<Acteur> casting = new ArrayList<>();
                    // ✅ movies.add() not movies = new Movie()
                    movies.add(new Movie(
                            currentId,
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("releaseYear"),
                            rs.getDouble("averageRating"),
                            rs.getString("coverImageUrl"),
                            rs.getString("backdrop_path"),
                            rs.getString("director"),
                            rs.getString("type"),
                            genreList,
                            casting,
                            rs.getInt("views"),
                            rs.getString("videoUrl"),
                            rs.getInt("duration_minutes")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur findbyGenre : " + e.getMessage());
            e.printStackTrace();
        }
        return movies;
    }

    public static Movie getMovieById(int id) {
        Movie movie = null;
        List<Acteur> casting = new ArrayList<>();
        String sql = "SELECT m.*, v.videoUrl, v.duration_minutes " +
                "FROM media m " +
                "INNER JOIN movie v ON m.id_Media = v.id_Media " +
                "WHERE m.id_Media = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    List<Genre> genreList = MediaDAO.getGenresByMediaId(id);
                    movie = new Movie(
                            rs.getInt("id_Media"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("releaseYear"),
                            rs.getDouble("averageRating"),
                            rs.getString("coverImageUrl"),
                            rs.getString("backdrop_path"),
                            rs.getString("director"),
                            rs.getString("type"),
                            genreList,
                            casting,
                            rs.getInt("views"),
                            rs.getString("videoUrl"),
                            rs.getInt("duration_minutes")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur getMovieById : " + e.getMessage());
            e.printStackTrace();
        }
        return movie;
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
                while (rs.next()) { /* map here if needed */ }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }
}
