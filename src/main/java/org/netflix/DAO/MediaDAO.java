package org.netflix.DAO;

import org.netflix.Models.Genre;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.*;

public class MediaDAO {
    private static Connection conn = ConxDB.getInstance();
    public static List<Genre> getGenresByMediaId(int mediaId) {
        List<Genre> genres = new ArrayList<>();

        String sql = "SELECT id_Genre FROM media_genres WHERE id_Media = " + mediaId;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                genres.add(new Genre(rs.getInt("id_Genre")));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching genres for Media ID " + mediaId);
        }
        return genres;
    }
    /*
     * nbr des media 7aseb l categori
     */
    public static Map<String, Integer> getMediaCountByCategory() {
        Map<String, Integer> stats = new HashMap<>();
        // Cette requête lie la table media aux catégories pour compter les occurrences
        String sql = "SELECT c.name, COUNT(m.id_Media) as total " +
                "FROM media m " +
                "JOIN category c ON m.id_Category = c.id_Category " +
                "GROUP BY c.name";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("name"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /*
      njibou akther 5 aflem metfarjin fehm
     */
    public static Map<String, Integer> getTop5Movies() {
        Map<String, Integer> top5 = new LinkedHashMap<>(); // LinkedHashMap pour garder l'ordre
        String sql = "SELECT title, views FROM media WHERE type = 'movie' ORDER BY views DESC LIMIT 5";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                top5.put(rs.getString("title"), rs.getInt("views"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top5;
    }
    //incrementation mta3 views

    public static void incrementViews(int idMedia) {
        String sql = "UPDATE media SET views = views + 1 WHERE id_Media = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idMedia);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'incrémentation des vues : " + e.getMessage());
        }
    }
}
