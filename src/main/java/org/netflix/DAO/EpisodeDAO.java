package org.netflix.DAO;

import org.netflix.Models.Episode;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EpisodeDAO {
    private static Connection conn = ConxDB.getInstance();

    /**
    les episode mta3 season
     */
    public static List<Episode> getEpisodesBySeason(int idSaison) {
        List<Episode> episodes = new ArrayList<>();
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
                            rs.getString("videoUrl"), // filePath dans votre modèle Episode
                            rs.getString("thumbnail_path")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des épisodes : " + e.getMessage());
        }
        return episodes;
    }

    /**
     ajout d'une episode
     */
    public static boolean addEpisode(Episode episode) {
        String sql = "INSERT INTO episode (id_Saison, episodeNumber, title, videoUrl, thumbnail_path) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, episode.getSeasonId());
            pstmt.setInt(2, episode.getEpisodeNumber());
            pstmt.setString(3, episode.getTitle());
            pstmt.setString(4, episode.getFilePath());
            pstmt.setString(5, episode.getThumbnailPath());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprime un épisode par son ID
     */
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

    /**
     * Récupère un épisode spécifique par son ID
     */
    public static Episode getEpisodeById(int idEpisode) {
        String sql = "SELECT * FROM episode WHERE id_Episode = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idEpisode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Episode(
                            rs.getInt("id_Episode"),
                            rs.getInt("id_Saison"),
                            rs.getInt("episodeNumber"),
                            rs.getString("title"),
                            rs.getString("videoUrl"),
                            rs.getString("thumbnail_path")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

        public List<Episode> findBySeasonId(int seasonId) {
            String sql = "SELECT * FROM episode WHERE season_id=? ORDER BY episode_number";
            return null;
        }

        public Episode findById(int episodeId) {
            String sql = "SELECT * FROM episode WHERE id=?";
            return null;
        }

        // ✅ Utilisé par SerieService pour le binge-watching
        public Episode findBySeasonAndNumber(int seasonId, int episodeNumber) {
            String sql = "SELECT * FROM episode WHERE season_id=? AND episode_number=?";
            return null;
        }

        public boolean insert(Episode episode) {
            String sql = "INSERT INTO episode (season_id, episode_number, title, file_path, thumbnail_path) VALUES (?,?,?,?,?)";
            return false;
        }

        public boolean update(Episode episode) {
            String sql = "UPDATE episode SET episode_number=?, title=?, file_path=?, thumbnail_path=? WHERE id=?";
            return false;
        }

        public boolean delete(int episodeId) {
            String sql = "DELETE FROM episode WHERE id=?";
            return false;
        }
    }

