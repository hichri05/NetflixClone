package org.netflix.DAO;
import org.netflix.Models.Comment;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class CommentDAO {
    private static Connection conn = ConxDB.getInstance();
    public static boolean addComment(Comment comment) {
        String sql = "INSERT INTO Comment (id_User, id_Media, content, created_at, is_reported) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, comment.getId_User());
            pstmt.setInt(2, comment.getId_Media());
            pstmt.setString(3, comment.getContent());
            pstmt.setDate(4, Date.valueOf(comment.getCreated_at()));
            pstmt.setInt(5, comment.getIs_reported());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static List<Comment> getCommentsByMedia(int id_Media) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM Comment WHERE id_Media = ? ORDER BY created_at DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_Media);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(new Comment(
                            rs.getInt("id_Comment"),
                            rs.getInt("id_User"),
                            rs.getInt("id_Media"),
                            rs.getString("content"),
                            rs.getDate("created_at").toLocalDate(),
                            rs.getInt("is_reported")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }
    public static boolean reportComment(int id_Comment) {
        String sql = "UPDATE Comment SET is_reported = 1 WHERE id_Comment = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_Comment);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static List<Comment> getReportedComments() {
        List<Comment> reportedComments = new ArrayList<>();
        String sql = "SELECT * FROM Comment WHERE is_reported = 1";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reportedComments.add(new Comment(
                        rs.getInt("id_Comment"),
                        rs.getInt("id_User"),

                        rs.getInt("id_Media"),
                        rs.getString("content"),
                        rs.getDate("created_at").toLocalDate(),
                        rs.getInt("is_reported")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reportedComments;
    }
    public static boolean deleteComment(int id_Comment) {
        String sql = "DELETE FROM Comment WHERE id_Comment = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_Comment);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Dans CommentDAO.java - Ajouter cette méthode

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM comments";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

