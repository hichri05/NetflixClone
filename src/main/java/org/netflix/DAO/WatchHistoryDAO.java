package org.netflix.DAO;

import org.netflix.Models.Media;
import org.netflix.Models.WatchHistory;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.*;

public class WatchHistoryDAO {
    private static Connection conn = ConxDB.getInstance();

    // ── EXISTING METHODS ─────────────────────────────────────────────

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

    // ── RECOMMENDATION SQL ───────────────────────────────────────────

    /**
     * Genre scores from watch history:
     * completed = +3, not completed = +1
     */
    public static Map<Integer, Integer> getGenreScoresFromWatchHistory(int userId) {
        Map<Integer, Integer> scores = new HashMap<>();
        String sql = "SELECT mg.id_Genre, wh.completed " +
                "FROM watch_history wh " +
                "JOIN media_genres mg ON wh.id_Media = mg.id_Media " +
                "WHERE wh.id_User = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int genreId = rs.getInt("id_Genre");
                int weight = rs.getInt("completed") == 1 ? 3 : 1;
                scores.merge(genreId, weight, Integer::sum);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return scores;
    }

    /**
     * Genre scores from favorites: +2 per genre
     */
    public static Map<Integer, Integer> getGenreScoresFromFavorites(int userId) {
        Map<Integer, Integer> scores = new HashMap<>();
        String sql = "SELECT mg.id_Genre FROM favorite f " +
                "JOIN media_genres mg ON f.id_Media = mg.id_Media " +
                "WHERE f.id_User = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                scores.merge(rs.getInt("id_Genre"), 2, Integer::sum);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return scores;
    }

    /**
     * Genre scores from ratings:
     * score >= 7 = +2, score >= 5 = +1
     */
    public static Map<Integer, Integer> getGenreScoresFromRatings(int userId) {
        Map<Integer, Integer> scores = new HashMap<>();
        String sql = "SELECT mg.id_Genre, r.score FROM rating r " +
                "JOIN media_genres mg ON r.id_Media = mg.id_Media " +
                "WHERE r.id_User = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int genreId = rs.getInt("id_Genre");
                double score = rs.getDouble("score");
                int weight = score >= 7 ? 2 : score >= 5 ? 1 : 0;
                if (weight > 0) scores.merge(genreId, weight, Integer::sum);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return scores;
    }

    /**
     * IDs of media user already watched or favorited
     */
    public static Set<Integer> getSeenMediaIds(int userId) {
        Set<Integer> seen = new HashSet<>();
        String sql = "SELECT id_Media FROM watch_history WHERE id_User = ? " +
                "UNION SELECT id_Media FROM favorite WHERE id_User = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) seen.add(rs.getInt("id_Media"));
        } catch (SQLException e) { e.printStackTrace(); }
        return seen;
    }

    /**
     * Media matching given genres, excluding seen, ordered by rating
     */
    public static List<Media> getMediaByGenres(List<Integer> genreIds, Set<Integer> exclude, int limit) {
        List<Media> result = new ArrayList<>();
        if (genreIds.isEmpty()) return result;

        String placeholders = String.join(",", Collections.nCopies(genreIds.size(), "?"));
        String excludeClause = exclude.isEmpty() ? "" :
                "AND m.id_Media NOT IN (" + String.join(",", Collections.nCopies(exclude.size(), "?")) + ") ";
        String sql = "SELECT DISTINCT m.* FROM media m " +
                "JOIN media_genres mg ON m.id_Media = mg.id_Media " +
                "WHERE mg.id_Genre IN (" + placeholders + ") " +
                excludeClause +
                "ORDER BY m.averageRating DESC LIMIT ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (int gId : genreIds) ps.setInt(i++, gId);
            for (int eId : exclude) ps.setInt(i++, eId);
            ps.setInt(i, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(mapMedia(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    /**
     * Top rated media not yet seen
     */
    public static List<Media> getTopRatedUnseen(Set<Integer> exclude, int limit) {
        List<Media> result = new ArrayList<>();
        String excludeClause = exclude.isEmpty() ? "" :
                "WHERE id_Media NOT IN (" + String.join(",", Collections.nCopies(exclude.size(), "?")) + ") ";
        String sql = "SELECT * FROM media " + excludeClause + "ORDER BY averageRating DESC LIMIT ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (int eId : exclude) ps.setInt(i++, eId);
            ps.setInt(i, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(mapMedia(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    /**
     * Trending fallback — most viewed, excluding already added
     */
    public static List<Media> getTrending(Set<Integer> exclude, int limit) {
        List<Media> result = new ArrayList<>();
        String excludeClause = exclude.isEmpty() ? "" :
                "WHERE id_Media NOT IN (" + String.join(",", Collections.nCopies(exclude.size(), "?")) + ") ";
        String sql = "SELECT * FROM media " + excludeClause + "ORDER BY views DESC LIMIT ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (int eId : exclude) ps.setInt(i++, eId);
            ps.setInt(i, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(mapMedia(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // ── MAPPER ───────────────────────────────────────────────────────

    private static Media mapMedia(ResultSet rs) throws SQLException {
        return new Media(
                rs.getInt("id_Media"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("releaseYear"),
                rs.getDouble("averageRating"),
                rs.getString("coverImageUrl"),
                rs.getString("backdrop_path"),
                rs.getString("director"),
                new ArrayList<>(),
                new ArrayList<>(),
                rs.getInt("views"),
                rs.getString("type")
        );
    }
}