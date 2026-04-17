package org.netflix.DAO;

import org.netflix.Models.Comment;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {
    private static Connection conn = ConxDB.getInstance();

    // ── DTO ───────────────────────────────────────────────────────────────────
    public static class CommentDTO {
        public Comment comment;
        public String username;
        public String mediaTitle; // ← NEW: media title for admin view

        public CommentDTO(Comment comment, String username, String mediaTitle) {
            this.comment = comment;
            this.username = username;
            this.mediaTitle = mediaTitle != null ? mediaTitle : "Unknown";
        }

        // Backwards-compatible constructor (used in MediaDetailsController)
        public CommentDTO(Comment comment, String username) {
            this(comment, username, "");
        }
    }

    // ── Add comment ───────────────────────────────────────────────────────────
    public static boolean addComment(Comment comment) {
        String sql = "INSERT INTO comment (id_User, id_Media, content, created_at, is_reported) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, comment.getId_User());
            pstmt.setInt(2, comment.getId_Media());
            pstmt.setString(3, comment.getContent());
            pstmt.setDate(4, Date.valueOf(
                    comment.getCreated_at() != null ? comment.getCreated_at() : LocalDate.now()));
            pstmt.setInt(5, comment.getIs_reported());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Get comments by media (for MediaDetailsController) ───────────────────
    public static List<CommentDTO> getCommentsByMedia(int id_Media) {
        List<CommentDTO> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.userName, m.title AS mediaTitle " +
                "FROM comment c " +
                "JOIN user u ON c.id_User = u.id_User " +
                "JOIN media m ON c.id_Media = m.id_Media " +
                "WHERE c.id_Media = ? ORDER BY c.created_at DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_Media);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Comment c = mapComment(rs);
                    comments.add(new CommentDTO(c, rs.getString("userName"), rs.getString("mediaTitle")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    // ── Get ALL comments (for admin — all media) ──────────────────────────────
    public static List<CommentDTO> getAllComments() {
        List<CommentDTO> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.userName, m.title AS mediaTitle " +
                "FROM comment c " +
                "JOIN user u ON c.id_User = u.id_User " +
                "JOIN media m ON c.id_Media = m.id_Media " +
                "ORDER BY c.created_at DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Comment c = mapComment(rs);
                comments.add(new CommentDTO(c, rs.getString("userName"), rs.getString("mediaTitle")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    // ── Get reported comments as DTO (for admin tab) ──────────────────────────
    public static List<CommentDTO> getReportedCommentsDTO() {
        List<CommentDTO> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.userName, m.title AS mediaTitle " +
                "FROM comment c " +
                "JOIN user u ON c.id_User = u.id_User " +
                "JOIN media m ON c.id_Media = m.id_Media " +
                "WHERE c.is_reported = 1 ORDER BY c.created_at DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Comment c = mapComment(rs);
                comments.add(new CommentDTO(c, rs.getString("userName"), rs.getString("mediaTitle")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    // ── Report a comment (called from MediaDetailsController) ─────────────────
    public static boolean reportComment(int id_Comment) {
        String sql = "UPDATE comment SET is_reported = 1 WHERE id_Comment = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_Comment);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean UnreportComment(int id_Comment) {
        String sql = "UPDATE comment SET is_reported = 0 WHERE id_Comment = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_Comment);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // ── Dismiss a report (admin clears the flag) ──────────────────────────────
    public static boolean dismissReport(int id_Comment) {
        String sql = "UPDATE comment SET is_reported = 0 WHERE id_Comment = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_Comment);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Delete a comment ──────────────────────────────────────────────────────
    public static boolean deleteComment(int id_Comment) {
        String sql = "DELETE FROM comment WHERE id_Comment = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_Comment);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Legacy list (kept for backward compat) ────────────────────────────────
    public static List<Comment> getReportedComments() {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT * FROM comment WHERE is_reported = 1";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapComment(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM comment";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ── Private mapper ────────────────────────────────────────────────────────
    private static Comment mapComment(ResultSet rs) throws SQLException {
        return new Comment(
                rs.getInt("id_Comment"),
                rs.getInt("id_User"),
                rs.getInt("id_Media"),
                rs.getString("content"),
                rs.getDate("created_at") != null
                        ? rs.getDate("created_at").toLocalDate()
                        : LocalDate.now(),
                rs.getInt("is_reported")
        );
    }

    // ── Stub methods kept for CommentServiceImpl ──────────────────────────────
    public boolean insert(Comment comment) { return addComment(comment); }
    public List<Comment> findByMedia(int mediaId) {
        List<Comment> out = new ArrayList<>();
        getCommentsByMedia(mediaId).forEach(dto -> out.add(dto.comment));
        return out;
    }
    public boolean delete(int commentId) { return deleteComment(commentId); }
    public boolean markAsReported(int commentId) { return reportComment(commentId); }
    public List<Comment> findReported() { return getReportedComments(); }
}
