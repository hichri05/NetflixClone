// package org.netflix.DAO;

package org.netflix.DAO;

import org.netflix.Models.MyList;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MyListDAO {

    // ==================== INSERTION ET SUPPRESSION ====================

    public boolean insert(MyList myListItem) {
        String sql = "INSERT INTO my_list (user_id, movie_id, serie_id, added_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, myListItem.getUserId());

            if (myListItem.getMovieId() != null) {
                pstmt.setInt(2, myListItem.getMovieId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            if (myListItem.getSerieId() != null) {
                pstmt.setInt(3, myListItem.getSerieId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            pstmt.setTimestamp(4, myListItem.getAddedAt());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMovie(int userId, int movieId) {
        String sql = "DELETE FROM my_list WHERE user_id = ? AND movie_id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, movieId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSerie(int userId, int serieId) {
        String sql = "DELETE FROM my_list WHERE user_id = ? AND serie_id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, serieId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteByUser(int userId) {
        String sql = "DELETE FROM my_list WHERE user_id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== RECHERCHE ====================

    public List<Integer> findMovieIdsByUser(int userId) {
        String sql = "SELECT movie_id FROM my_list WHERE user_id = ? AND movie_id IS NOT NULL ORDER BY added_at DESC";
        List<Integer> movieIds = new ArrayList<>();

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movieIds.add(rs.getInt("movie_id"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieIds;
    }

    public List<Integer> findSerieIdsByUser(int userId) {
        String sql = "SELECT serie_id FROM my_list WHERE user_id = ? AND serie_id IS NOT NULL ORDER BY added_at DESC";
        List<Integer> serieIds = new ArrayList<>();

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    serieIds.add(rs.getInt("serie_id"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return serieIds;
    }

    public Optional<MyList> findByUserAndMovie(int userId, int movieId) {
        String sql = "SELECT * FROM my_list WHERE user_id = ? AND movie_id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, movieId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMyList(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<MyList> findByUserAndSerie(int userId, int serieId) {
        String sql = "SELECT * FROM my_list WHERE user_id = ? AND serie_id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, serieId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMyList(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // ==================== VÉRIFICATIONS ====================

    public boolean isMovieInList(int userId, int movieId) {
        String sql = "SELECT COUNT(*) FROM my_list WHERE user_id = ? AND movie_id = ?";

        try (Connection conn =ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, movieId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isSerieInList(int userId, int serieId) {
        String sql = "SELECT COUNT(*) FROM my_list WHERE user_id = ? AND serie_id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, serieId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== STATISTIQUES ====================

    public int countMoviesByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM my_list WHERE user_id = ? AND movie_id IS NOT NULL";

        try (Connection conn = ConxDB.getConnection();
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

    public int countSeriesByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM my_list WHERE user_id = ? AND serie_id IS NOT NULL";

        try (Connection conn = ConxDB.getConnection();
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

    public Optional<Timestamp> getAddedAt(int userId, int mediaId, String type) {
        String sql;
        if ("MOVIE".equalsIgnoreCase(type)) {
            sql = "SELECT added_at FROM my_list WHERE user_id = ? AND movie_id = ?";
        } else {
            sql = "SELECT added_at FROM my_list WHERE user_id = ? AND serie_id = ?";
        }

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, mediaId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getTimestamp("added_at"));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private MyList mapResultSetToMyList(ResultSet rs) throws SQLException {
        Integer movieId = rs.getObject("movie_id") != null ? rs.getInt("movie_id") : null;
        Integer serieId = rs.getObject("serie_id") != null ? rs.getInt("serie_id") : null;

        return new MyList(
                rs.getInt("user_id"),
                movieId,
                serieId,
                rs.getTimestamp("added_at")
        );
    }
}