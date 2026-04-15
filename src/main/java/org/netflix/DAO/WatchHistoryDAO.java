package org.netflix.DAO;

import org.netflix.Models.Media;
import org.netflix.Models.WatchHistory;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.*;

public class WatchHistoryDAO {
    private static Connection conn = ConxDB.getInstance();


    public static List<Object[]> getTopViewedMovies(int limit) {
        String sql = "SELECT id_Media, COUNT(*) as view_count FROM watch_history " +
                "WHERE id_Media IS NOT NULL AND id_Episode IS NULL " +
                "GROUP BY id_Media ORDER BY view_count DESC LIMIT ?";
        List<Object[]> results = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Object[]{rs.getInt("id_Media"), rs.getLong("view_count")});
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return results;
    }

    // Séries les plus vues (basé sur id_Episode)
    public static List<Object[]> getTopViewedSeries(int limit) {
        String sql = "SELECT id_Media, COUNT(*) as view_count FROM watch_history " +
                "WHERE id_Episode IS NOT NULL GROUP BY id_Media " +
                "ORDER BY view_count DESC LIMIT ?";
        List<Object[]> results = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Object[]{rs.getInt("id_Media"), rs.getLong("view_count")});
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return results;
    }

    // Temps moyen par utilisateur (colonne stopped_at_time)
    public static double getAverageWatchTimePerUser() {
        String sql = "SELECT AVG(total) FROM (SELECT SUM(stopped_at_time) as total " +
                "FROM watch_history GROUP BY id_User) as t";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static boolean addToHistory(WatchHistory h) {
        String sql = "INSERT INTO watch_history (id_User, id_Media, id_Episode, stopped_at_time, last_watched, completed) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, h.getUserId());
            if (h.getMediaId() != null) pstmt.setInt(2, h.getMediaId()); else pstmt.setNull(2, Types.INTEGER);
            if (h.getEpisodeId() != null) pstmt.setInt(3, h.getEpisodeId()); else pstmt.setNull(3, Types.INTEGER);
            pstmt.setDouble(4, h.getStoppedAtTime());
            pstmt.setTimestamp(5, h.getLastWatched());
            pstmt.setInt(6, h.getCompleted());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean isEpisodeCompleted(int userId, int id) {
        return true;}

    public List<WatchHistory> findByUser(int userId) {
        List<WatchHistory> historyList = new ArrayList<>();
        String sql = "SELECT * FROM watch_history WHERE id_User = ? ORDER BY last_watched DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Get these as Objects first to handle potential NULLs from the DB
                    Integer mediaId = (Integer) rs.getObject("id_Media");
                    Integer episodeId = (Integer) rs.getObject("id_Episode");

                    // Using your first constructor:
                    // (userId, mediaId, episodeId, stoppedAtTime, lastWatched, completed)
                    WatchHistory h = new WatchHistory(
                            rs.getInt("id_User"),
                            mediaId,
                            episodeId,
                            rs.getDouble("stopped_at_time"),
                            rs.getTimestamp("last_watched"),
                            rs.getInt("completed")
                    );

                    historyList.add(h);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in findByUser:");
            e.printStackTrace();
        }

        return historyList;
    }
}