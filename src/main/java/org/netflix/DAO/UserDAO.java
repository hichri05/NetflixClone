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

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String SQL = "SELECT id_User, userName, email FROM user";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                int id = rs.getInt("id_User");
                String username = rs.getString("userName");
                String email = rs.getString("email");

                User user = new User(id, username, email);
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
                "Inner JOIN favorite f ON m.id_Media = f.id_Media " +
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
                    String backdropImageUrl = rs.getString("backdrop_path");
                    String director = rs.getString("director");
                    favorites.add(MediaDAO.ResultToMedia(rs));

                    if ("movie".equalsIgnoreCase(type)) {

                        favorites.add(new Movie(
                                idMedia, title, description, releaseYear, averageRating,
                                coverImageUrl, backdropImageUrl, director, null, 0, new ArrayList<>(), new ArrayList<>()
                        ));
                    } else if ("serie".equalsIgnoreCase(type)) {
                        int nbrSaison = rs.getInt("nbrSaison");
                        favorites.add(new Serie(
                                idMedia, title, description, releaseYear, averageRating,
                                coverImageUrl, backdropImageUrl, director, nbrSaison, new ArrayList<>(), new ArrayList<>()
                        ));
                    }
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
        String sql = "SELECT id, username, email FROM user WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

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


}