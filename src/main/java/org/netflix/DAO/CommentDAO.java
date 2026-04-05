// package org.netflix.DAO;

package org.netflix.DAO;

import org.netflix.Models.Comment;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommentDAO {


    public boolean insert(Comment comment) {
        String sql = "INSERT INTO comments (user_id, media_id, content, reported, created_at) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, comment.getUserId());
            pstmt.setInt(2, comment.getMediaId());
            pstmt.setString(3, comment.getContent());
            pstmt.setBoolean(4, comment.isReported());
            pstmt.setTimestamp(5, comment.getCreatedAt());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        comment.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM comments WHERE id = ?";

        try (Connection conn =ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int deleteByUser(int userId) {
        String sql = "DELETE FROM comments WHERE user_id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int deleteByMedia(int mediaId) {
        String sql = "DELETE FROM comments WHERE media_id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mediaId);
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int deleteReportedComments() {
        String sql = "DELETE FROM comments WHERE reported = true";

        try (Connection conn = ConxDB.getConnection();
             Statement stmt = conn.createStatement()) {

            return stmt.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ==================== RECHERCHE ====================

    public Optional<Comment> findById(int id) {
        String sql = "SELECT * FROM comments WHERE id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToComment(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public List<Comment> findByMedia(int mediaId) {
        String sql = "SELECT * FROM comments WHERE media_id = ? ORDER BY created_at DESC";
        List<Comment> comments = new ArrayList<>();

        try (Connection conn =ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mediaId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public List<Comment> findByUser(int userId) {
        String sql = "SELECT * FROM comments WHERE user_id = ? ORDER BY created_at DESC";
        List<Comment> comments = new ArrayList<>();

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public List<Comment> findRecentComments(int limit) {
        String sql = "SELECT * FROM comments ORDER BY created_at DESC LIMIT ?";
        List<Comment> comments = new ArrayList<>();

        try (Connection conn =ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapResultSetToComment(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public List<Comment> findReportedComments() {
        String sql = "SELECT * FROM comments WHERE reported = true ORDER BY created_at DESC";
        List<Comment> comments = new ArrayList<>();

        try (Connection conn = ConxDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                comments.add(mapResultSetToComment(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    // ==================== SIGNALEMENT ====================

    public boolean markAsReported(int commentId) {
        String sql = "UPDATE comments SET reported = true WHERE id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, commentId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unmarkAsReported(int commentId) {
        String sql = "UPDATE comments SET reported = false WHERE id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, commentId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== STATISTIQUES ====================

    public int countByMedia(int mediaId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE media_id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mediaId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int countByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE user_id = ?";

        try (Connection conn =ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int countReported() {
        String sql = "SELECT COUNT(*) FROM comments WHERE reported = true";

        try (Connection conn = ConxDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countReportedByMedia(int mediaId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE media_id = ? AND reported = true";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mediaId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private Comment mapResultSetToComment(ResultSet rs) throws SQLException {
        return new Comment(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("media_id"),
                rs.getString("content"),
                rs.getBoolean("reported"),
                rs.getTimestamp("created_at")
        );
    }
}