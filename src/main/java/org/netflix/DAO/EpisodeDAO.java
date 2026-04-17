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
        String sql = "SELECT * FROM episode WHERE id_Saison = ? ORDER BY episodeNumber ASC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idSaison);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Episode ep = new Episode();
                    ep.setId(rs.getInt("id_Episode"));
                    ep.setSeasonId(rs.getInt("id_Saison"));
                    ep.setEpisodeNumber(rs.getInt("episodeNumber"));
                    ep.setTitle(rs.getString("title"));
                    ep.setDescription(rs.getString("description"));
                    ep.setFilePath(rs.getString("videoUrl"));
                    ep.setThumbnailPath(rs.getString("thumbnail_path"));
                    ep.setDuration(rs.getInt("duration_minutes"));

                    episodes.add(ep);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur DAO : " + e.getMessage());
        }
        return episodes;
    }

    public static boolean addEpisode(Episode episode) {
        String sql = "INSERT INTO episode (id_Saison, episodeNumber, title, description, videoUrl, thumbnail_path, duration_minutes, duration) VALUES (?, ?, ?, ?, ?, ?, 0, 0)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, episode.getSeasonId());
            pstmt.setInt(2, episode.getEpisodeNumber());
            pstmt.setString(3, episode.getTitle());
            pstmt.setString(4, episode.getDescription());
            pstmt.setString(5, episode.getFilePath());
            pstmt.setString(6, episode.getThumbnailPath());
            //pstmt.setInt(7, episode.getDuration());

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