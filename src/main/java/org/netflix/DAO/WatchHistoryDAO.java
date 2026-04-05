package org.netflix.DAO;

import org.netflix.Models.WatchHistory;
import org.netflix.Utils.ConxDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WatchHistoryDAO {
    private static Connection conn = ConxDB.getInstance();

    // Films les plus vus (basé sur id_Media)
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

    // Ajout d'un historique (respecte les colonnes de l'image 42e4dd)
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

    // package org.netflix.DAO;



        public Optional<WatchHistory> findByUserAndMovie(int userId, int movieId) {
            String sql = "SELECT * FROM watch_history WHERE user_id = ? AND movie_id = ?";

            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, userId);
                pstmt.setInt(2, movieId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToWatchHistory(rs));
                    }
                }
                return Optional.empty();

            } catch (SQLException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        }

        public Optional<WatchHistory> findByUserAndEpisode(int userId, int episodeId) {
            String sql = "SELECT * FROM watch_history WHERE user_id = ? AND episode_id = ?";

            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, userId);
                pstmt.setInt(2, episodeId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToWatchHistory(rs));
                    }
                }
                return Optional.empty();

            } catch (SQLException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        }

        public List<WatchHistory> findByUser(int userId) {
            String sql = "SELECT * FROM watch_history WHERE user_id = ? ORDER BY last_watched DESC";
            List<WatchHistory> histories = new ArrayList<>();

            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, userId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        histories.add(mapResultSetToWatchHistory(rs));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return histories;
        }

        public boolean insert(WatchHistory history) {
            String sql = "INSERT INTO watch_history (user_id, movie_id, episode_id, stopped_at_time, last_watched) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, history.getUserId());

                if (history.getMovieId() != null) {
                    pstmt.setInt(2, history.getMovieId());
                } else {
                    pstmt.setNull(2, Types.INTEGER);
                }

                if (history.getEpisodeId() != null) {
                    pstmt.setInt(3, history.getEpisodeId());
                } else {
                    pstmt.setNull(3, Types.INTEGER);
                }

                pstmt.setDouble(4, history.getStoppedAtTime());
                pstmt.setTimestamp(5, history.getLastWatched());

                return pstmt.executeUpdate() > 0;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean update(WatchHistory history) {
            String sql = "UPDATE watch_history SET stopped_at_time = ?, last_watched = ? " +
                    "WHERE user_id = ? AND movie_id IS NOT NULL AND movie_id = ?";

            // Pour les films
            if (history.getMovieId() != null) {
                try (Connection conn = ConxDB.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setDouble(1, history.getStoppedAtTime());
                    pstmt.setTimestamp(2, history.getLastWatched());
                    pstmt.setInt(3, history.getUserId());
                    pstmt.setInt(4, history.getMovieId());

                    return pstmt.executeUpdate() > 0;

                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            // Pour les épisodes
            if (history.getEpisodeId() != null) {
                String sqlEpisode = "UPDATE watch_history SET stopped_at_time = ?, last_watched = ? " +
                        "WHERE user_id = ? AND episode_id = ?";
                try (Connection conn = ConxDB.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sqlEpisode)) {

                    pstmt.setDouble(1, history.getStoppedAtTime());
                    pstmt.setTimestamp(2, history.getLastWatched());
                    pstmt.setInt(3, history.getUserId());
                    pstmt.setInt(4, history.getEpisodeId());

                    return pstmt.executeUpdate() > 0;

                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            return false;
        }

        private WatchHistory mapResultSetToWatchHistory(ResultSet rs) throws SQLException {
            Integer movieId = rs.getObject("movie_id") != null ? rs.getInt("movie_id") : null;
            Integer episodeId = rs.getObject("episode_id") != null ? rs.getInt("episode_id") : null;

            return new WatchHistory(
                    rs.getInt("user_id"),
                    movieId,
                    episodeId,
                    rs.getDouble("stopped_at_time"),
                    rs.getTimestamp("last_watched")
            );
        }
    }
}