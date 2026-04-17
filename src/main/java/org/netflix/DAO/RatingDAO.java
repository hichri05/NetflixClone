package org.netflix.DAO;
import org.netflix.Models.Rating;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RatingDAO {
    private static Connection conn = ConxDB.getInstance();
    public static boolean saveOrUpdateRating(Rating rating) {
        String sql = "INSERT INTO rating (id_User, id_Media, score, ratingDate) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE score = ?, ratingDate = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rating.getId_User());
            pstmt.setInt(2, rating.getId_Media());
            pstmt.setFloat(3, rating.getScore());
            pstmt.setDate(4, Date.valueOf(rating.getRatingDate()));

            pstmt.setFloat(5, rating.getScore());
            pstmt.setDate(6, Date.valueOf(rating.getRatingDate()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static double getAverageRating(int id_Media) {
        String sql = "SELECT AVG(score) as average FROM rating WHERE id_Media = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_Media);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("average");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    public static Rating getUserRating(int id_User, int id_Media) {
        String sql = "SELECT * FROM rating WHERE id_User = ? AND id_Media = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_User);
            pstmt.setInt(2, id_Media);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Rating(
                            rs.getInt("id_Rating"),
                            rs.getInt("id_User"),
                            rs.getInt("id_Media"),
                            rs.getFloat("score"),
                            rs.getTimestamp("ratingDate").toLocalDateTime().toLocalDate()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static List<Integer> getTopRatedMediaIds(int limit) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id_Media, AVG(score) as avg_score FROM rating GROUP BY id_Media ORDER BY avg_score DESC LIMIT ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id_Media"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    public Rating findByUserAndMedia(int userId, int mediaId) {
        String sql = "SELECT * FROM rating WHERE id_user=? AND id_media=?";
        return null;
    }

    public static List<Rating> findByMedia(int mediaId) {
        String sql = "SELECT * FROM rating WHERE id_media=?";
        return null;
    }

    public boolean insert(Rating rating) {
        String sql = "INSERT INTO rating (id_user, id_media, score, rating_date) VALUES (?,?,?,?)";
        return false;
    }

    public boolean update(Rating rating) {
        String sql = "UPDATE rating SET score=?, rating_date=? WHERE id_user=? AND id_media=?";
        return false;
    }
}
