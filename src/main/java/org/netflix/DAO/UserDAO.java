package org.netflix.DAO;

import org.netflix.Models.User;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {
    private static Connection conn = ConxDB.getInstance();

    public static List<User> getAllUsers()
    {
        Statement stmt = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<User>();
        String SQL = "SELECT * FROM users";
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(SQL);

            while (rs.next()){
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String Email = rs.getString("email");

                User user = new User(id, username, Email);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }  finally {
            try {
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return users;
    }
    // Dans UserDAO.java - Ajouter cette méthode si non existante
    public Optional<User> findById(int id) {
        String sql = "SELECT id, username, email FROM users WHERE id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email")
                    ));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    public Optional<User> insert(User user, String hashedPassword) {
        String sql = "INSERT INTO users (username, email, password_hash, role, created_at) " +
                "VALUES (?, ?, ?, 'USER', NOW())";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, hashedPassword);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        return Optional.of(new User(generatedId, user.getUsername(), user.getEmail()));
                    }
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, username, email FROM users WHERE email = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email")
                    ));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<String> getPasswordHash(int userId) {
        String sql = "SELECT password_hash FROM users WHERE id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString("password_hash"));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn =ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

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
    public boolean updatePassword(int userId, String newHashedPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newHashedPassword);
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean isAdmin(int userId) {
        String sql = "SELECT role FROM users WHERE id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return "ADMIN".equals(rs.getString("role"));
                }
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public String getUserRole(int userId) {
        String sql = "SELECT role FROM users WHERE id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
            return "USER";

        } catch (SQLException e) {
            e.printStackTrace();
            return "USER";
        }
    }
}

