package org.netflix.DAO;

import org.netflix.Models.Genre;
import org.netflix.Models.MediaGenre;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GenreDAO {

    private static Connection conn = ConxDB.getInstance();
    public static List<Genre> getGenresByMedia(int mediaId) {
        List<Genre> genres = new ArrayList<>();
        String sql = "SELECT g.id_Genre, g.name " +
                "FROM genres g " +
                "JOIN media_genres mg ON g.id_Genre = mg.id_Genre " +
                "WHERE mg.id_Media = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mediaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id_Genre");
                MediaGenre mediaGenre = MediaGenre.fromString(rs.getString("name"));
                if (mediaGenre != null) {
                    genres.add(new Genre(id, mediaGenre));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }

    public static List<Genre> getAllGenres() {
        List<Genre> genres = new ArrayList<>();
        String sql = "SELECT id_Genre, name FROM genres";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id_Genre");
                MediaGenre mediaGenre = MediaGenre.fromString(rs.getString("name"));
                if (mediaGenre != null) {
                    genres.add(new Genre(id, mediaGenre));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }
}