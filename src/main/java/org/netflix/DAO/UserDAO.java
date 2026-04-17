package org.netflix.DAO;

import org.netflix.Models.Media;
import org.netflix.Models.User;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.*;

public class UserDAO {
    private static Connection conn = ConxDB.getInstance();

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        String SQL = "SELECT id_User, userName, email, role FROM user";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                int id           = rs.getInt("id_User");
                String username  = rs.getString("userName");
                String email     = rs.getString("email");
                String role      = rs.getString("role");

                User user = new User(id, username, email, role, null, new ArrayList<>());
                users.add(user);
            }

            System.out.println("DEBUG: Successfully loaded " + users.size() + " users.");

        } catch (SQLException e) {
            System.err.println("SQL Error in getAllUsers: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    public static List<Media> getUserFavorites(int userId) {
        List<Media> favorites = new ArrayList<>();
        String sql = "SELECT m.* FROM media m " +
                "INNER JOIN favorite f ON m.id_Media = f.id_Media " +
                "WHERE f.id_User = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Media media = MediaDAO.ResultToMedia(rs);
                    media.setGenres(MediaDAO.getGenresByMediaId(media.getIdMedia()));
                    favorites.add(media);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des favoris : " + e.getMessage());
        }
        return favorites;
    }

    public static String getHashedPass(String email) {
        String sql = "SELECT password FROM user WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User findByEmail(String email) {
        String sql = "SELECT id_User, userName, email, role FROM user WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id_User"),
                            rs.getString("userName"),
                            rs.getString("email"),
                            rs.getString("role"),
                            null,
                            new ArrayList<>()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean AddUser(User newUser) {
        String sql = "INSERT INTO user (userName, email, password, role) VALUES (?, ?, ?, 'user')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newUser.getUsername());
            pstmt.setString(2, newUser.getEmail());
            pstmt.setString(3, newUser.getPassword());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isFavorite(int userId, int mediaId) {
        String sql = "SELECT COUNT(*) FROM favorite WHERE id_user = ? AND id_media = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, mediaId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateRole(int idUser, String newRole) {
        String sql = "UPDATE user SET role = ? WHERE id_User = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole);
            pstmt.setInt(2, idUser);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du rôle : " + e.getMessage());
        }
        return false;
    }

    public static boolean deleteUser(int idUser) {
        String sql = "DELETE FROM user WHERE id_User = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUser);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
            return false;
        }
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT id_User, userName, email, role FROM user WHERE id_User = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getInt("id_User"),
                            rs.getString("userName"),
                            rs.getString("email"),
                            rs.getString("role"),
                            null,
                            new ArrayList<>()
                    ));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    public static Map<String, Long> getUsersGroupedByDate() {
        Map<String, Long> result = new LinkedHashMap<>();
        String sql = "SELECT DATE(createdAt) AS reg_date, COUNT(*) AS cnt " +
                "FROM user " +
                "WHERE createdAt IS NOT NULL " +
                "GROUP BY DATE(createdAt) " +
                "ORDER BY reg_date ASC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String date = rs.getString("reg_date");
                long   count = rs.getLong("cnt");
                result.put(date, count);
            }

        } catch (SQLException e) {
            System.err.println("Error in getUsersGroupedByDate: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public static List<Media> getWatchHistory(int userId) {
        List<Media> history = new ArrayList<>();
        String sql = """
            SELECT m.* FROM media m
            JOIN watch_history wh ON m.id_Media = wh.id_Media
            WHERE wh.id_User = ?
            ORDER BY wh.last_watched DESC
            LIMIT 20
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(MediaDAO.ResultToMedia(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'historique : " + e.getMessage());
            e.printStackTrace();
        }
        return history;
    }
}
