package org.netflix.DAO;

import org.netflix.Models.Genre;
import org.netflix.Utils.ConxDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MediaDAO {
    private static Connection conn = ConxDB.getInstance();
    public static List<Genre> getGenresByMediaId(int mediaId) {
        List<Genre> genres = new ArrayList<>();

        String sql = "SELECT id_Genre FROM media_genres WHERE id_Media = " + mediaId;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                genres.add(new Genre(rs.getInt("id_Genre")));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching genres for Media ID " + mediaId);
        }
        return genres;
    }
}
