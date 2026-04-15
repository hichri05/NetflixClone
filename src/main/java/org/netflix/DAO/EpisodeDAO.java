package org.netflix.DAO;

import org.netflix.Models.Episode;
import org.netflix.Utils.ConxDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EpisodeDAO {
    private static Connection conn = ConxDB.getInstance();

    public static List<Episode> getEpisodesBySeason(int idSaison) {
        List<Episode> episodes = new ArrayList<>();
        // Fixed: Column names match your DB image (id_Saison, episodeNumber)
        String sql = "SELECT * FROM episode WHERE id_Saison = ? ORDER BY episodeNumber ASC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idSaison);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    episodes.add(new Episode(
                            rs.getInt("id_Episode"),
                            rs.getInt("id_Saison"),
                            rs.getInt("episodeNumber"),
                            rs.getString("title"),
                            rs.getString("videoUrl"),
                            rs.getString("thumbnail_path")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
        return episodes;
    }

    public static boolean addEpisode(Episode episode) {
        // Fixed: matches columns in your screenshot
        String sql = "INSERT INTO episode (id_Saison, episodeNumber, title, videoUrl, thumbnail_path, description, duration_minutes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, episode.getSeasonId());
            pstmt.setInt(2, episode.getEpisodeNumber());
            pstmt.setString(3, episode.getTitle());
            pstmt.setString(4, episode.getFilePath());
            pstmt.setString(5, episode.getThumbnailPath());
            pstmt.setString(6, episode.getDescription());
            pstmt.setInt(7, episode.getDuration());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteEpisode(int idEpisode) {
        String sql = "DELETE FROM episode WHERE id_Episode = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idEpisode);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}