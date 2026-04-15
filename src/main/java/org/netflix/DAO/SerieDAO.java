package org.netflix.DAO;
import org.netflix.Models.Acteur;
import org.netflix.Models.Genre;
import org.netflix.Models.Movie;
import org.netflix.Models.Serie;
import org.netflix.Utils.ConxDB;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;

public class SerieDAO {
    private static Connection  con=ConxDB.getInstance();
    public List<Serie> getAllSeries() {
        List<Serie> series = new ArrayList<>();
        // FIX 1: Added spaces at the end of each string line to prevent "s.nbrSaisonFROM" error
        // FIX 2: Corrected table names to match your schema (media and serie)
        String sql = "SELECT m.*, s.nbrSaison " +
                "FROM media m " +
                "INNER JOIN serie s ON m.id_Media = s.id_Media";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Fetch genres for this specific media
                List<Genre> genresList = MediaDAO.getGenresByMediaId(rs.getInt("id_Media"));
                List<Acteur> casting = new ArrayList<>(); // Placeholder as seen in your code

                // FIX 3: Defensive Null Checks for the Chart
                // If views or backdrop_path are missing in DB, we provide defaults
                int views = 0;
                try { views = rs.getInt("views"); } catch (SQLException e) { /* Column might not exist */ }

                String backdrop = rs.getString("backdrop_path");
                if (backdrop == null) backdrop = rs.getString("coverImageUrl");

                series.add(new Serie(
                        rs.getInt("id_Media"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("releaseYear"),
                        rs.getDouble("averageRating"),
                        rs.getString("coverImageUrl"),
                        backdrop,
                        rs.getString("director"),
                        rs.getString("type"),
                        rs.getInt("nbrSaison"),
                        genresList,
                        casting,
                        views
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error in getAllSeries: " + e.getMessage());
            e.printStackTrace();
        }
        return series;
    }
    public boolean updateRating(Serie serie) {
        String sql = "UPDATE series SET average_rating = ? WHERE id = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setDouble(1, serie.getAverageRating());
            pstmt.setInt(2, serie.getIdMedia());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Serie> findTopRated(int limit) {
        String sql = "SELECT * FROM series ORDER BY average_rating DESC LIMIT ?";
        List<Serie> series = new ArrayList<>();

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    //series.add(mapResultSetToSerie(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return series;
    }


    public boolean updateSeasonCount(int serieId, int seasonCount) {
        String sql = "UPDATE series SET number_of_seasons = ? WHERE id = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, seasonCount);
            pstmt.setInt(2, serieId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static List<Serie> findbyGenre(String genreName) {
        List<Serie> series = new ArrayList<>();

        String sql = "SELECT m.*, v.nbrSaison " +
                "FROM media m " +
                "INNER JOIN Serie v ON m.id_Media = v.id_Media " +
                "INNER JOIN media_genres mg ON m.id_Media = mg.id_Media " +
                "INNER JOIN genres g ON mg.id_Genre = g.id_Genre " +
                "WHERE g.name = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, genreName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    List<Genre> genresList = MediaDAO.getGenresByMediaId(rs.getInt("id_Media"));
                    List<Acteur> casting = new ArrayList<>();
                    series.add(new Serie(
                            rs.getInt("id_Media"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("releaseYear"),
                            rs.getDouble("averageRating"),
                            rs.getString("coverImageUrl"),
                            rs.getString("backdrop_path"),
                            rs.getString("director"),
                            rs.getString("type"),
                            rs.getInt("nbrSaison"),
                            genresList,
                            casting,
                            rs.getInt("views")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur findbyGenre : " + e.getMessage());
            e.printStackTrace();
        }
        return series;
    }

    private void insertSerieRow(int id) {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT IGNORE INTO serie (id_Media, nbrSaison) VALUES (?,0)")) {
            ps.setInt(1,id); ps.executeUpdate();
        } catch (SQLException e) { System.err.println(e.getMessage()); }
    }}
