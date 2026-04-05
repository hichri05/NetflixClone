// package org.netflix.DAO;

package org.netflix.DAO;

import org.netflix.Utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WatchProgressDAO {

    public Optional<WatchProgress> findByUserAndEpisode(int userId, int episodeId) {
        String sql = "SELECT * FROM watch_progress WHERE user_id = ? AND episode_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, episodeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToWatchProgress(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public List<WatchProgress> findByUser(int userId) {
        String sql = "SELECT * FROM watch_progress WHERE user_id = ? ORDER BY last_updated DESC";
        List<WatchProgress> progresses = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    progresses.add(mapResultSetToWatchProgress(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return progresses;
    }

    public boolean insert(WatchProgress progress) {
        String sql = "INSERT INTO watch_progress (user_id, episode_id, stopped_at_time, completed, last_updated) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, progress.getUserId());
            pstmt.setInt(2, progress.getEpisodeId());
            pstmt.setDouble(3, progress.getStoppedAtTime());
            pstmt.setBoolean(4, progress.isCompleted());
            pstmt.setTimestamp(5, progress.getLastUpdated());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(WatchProgress progress) {
        String sql = "UPDATE watch_progress SET stopped_at_time = ?, completed = ?, last_updated = ? " +
                "WHERE user_id = ? AND episode_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, progress.getStoppedAtTime());
            pstmt.setBoolean(2, progress.isCompleted());
            pstmt.setTimestamp(3, progress.getLastUpdated());
            pstmt.setInt(4, progress.getUserId());
            pstmt.setInt(5, progress.getEpisodeId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private WatchProgress mapResultSetToWatchProgress(ResultSet rs) throws SQLException {
        return new WatchProgress(
                rs.getInt("user_id"),
                rs.getInt("episode_id"),
                rs.getDouble("stopped_at_time"),
                rs.getBoolean("completed"),
                rs.getTimestamp("last_updated")
        );
    }
}