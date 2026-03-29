package org.netflix.DAO;

import org.netflix.Models.Media;
import org.netflix.Models.Movie;
import org.netflix.Models.Serie;
import org.netflix.Models.User;
import org.netflix.Utils.ConxDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
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

    public static List<Media> getUserFavorites(int userId) {
        List<Media> favorites = new ArrayList<>();
        String sql = "SELECT m.* FROM media m " +
                "JOIN favorite f ON m.id_Media = f.id_Media " +
                "WHERE f.id_User = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int idMedia = rs.getInt("id_Media");
                    String title = rs.getString("title");
                    String description = rs.getString("description");
                    int releaseYear = rs.getInt("releaseYear");
                    double averageRating = rs.getDouble("averageRating");
                    String coverImageUrl = rs.getString("coverImageUrl");
                    String backdropImageUrl =  rs.getString("backdrop_path");
                    String director = rs.getString("director");
                    String type = rs.getString("type");

                    if ("movie".equalsIgnoreCase(type)) {

                        favorites.add(new Movie(
                                idMedia, title, description, releaseYear, averageRating,
                                coverImageUrl,backdropImageUrl, director, null, 0, new ArrayList<>(), new ArrayList<>()
                        ));
                    } else if ("serie".equalsIgnoreCase(type)) {
                        int nbrSaison = rs.getInt("nbrSaison");
                        favorites.add(new Serie(
                                idMedia, title, description, releaseYear, averageRating,
                                coverImageUrl,backdropImageUrl, director, nbrSaison, new ArrayList<>(), new ArrayList<>()
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des favoris : " + e.getMessage());
        }
        return favorites;
    }

    public static User findByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Utilise ton constructeur User(id, username, email)
                    return new User(
                            rs.getInt("id_User"),
                            rs.getString("userName"),
                            rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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

    public static boolean AddUser(User newUser) {
        // Le rôle est 'user' par défaut selon ton ENUM
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

    public static boolean isFavorite(int idUser, int idMedia) {
        String sql = "SELECT * FROM favorite WHERE id_User = ? AND id_Media = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUser);
            pstmt.setInt(2, idMedia);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // true si le média est déjà en favori
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;

        }
    }
    public static boolean updateRole(int idUser, String newRole) {
        String sql = "UPDATE user SET role = ? WHERE id_User = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole);
            pstmt.setInt(2, idUser);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du rôle : " + e.getMessage());
            return false;
        }
    }
    /**
     *supp d'utilisateur
     */
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
}
