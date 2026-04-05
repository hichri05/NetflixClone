package org.netflix.DAO;

import org.netflix.Models.Episode;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public List<Episode> findBySerieId(int serieId) {
        String sql = "SELECT e.* FROM episodes e " +
                "JOIN seasons s ON e.season_id = s.id " +
                "WHERE s.serie_id = ? ORDER BY s.season_number, e.episode_number";
        List<Episode> episodes = new ArrayList<>();

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, serieId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    episodes.add(mapResultSetToEpisode(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return episodes;
    }
    public Optional<Episode> findBySeasonIdAndNumber(int seasonId, int episodeNumber) {
        String sql = "SELECT * FROM episodes WHERE season_id = ? AND episode_number = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, seasonId);
            pstmt.setInt(2, episodeNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEpisode(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public int countBySeasonId(int seasonId) {
        String sql = "SELECT COUNT(*) FROM episodes WHERE season_id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, seasonId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public boolean update(Episode episode) {
        String sql = "UPDATE episodes SET episode_number = ?, title = ?, file_path = ?, thumbnail_path = ? WHERE id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, episode.getEpisodeNumber());
            pstmt.setString(2, episode.getTitle());
            pstmt.setString(3, episode.getFilePath());
            pstmt.setString(4, episode.getThumbnailPath());
            pstmt.setInt(5, episode.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    private Episode mapResultSetToEpisode(ResultSet rs) throws SQLException {
        return new Episode(
                rs.getInt("id"),
                rs.getInt("season_id"),
                rs.getInt("episode_number"),
                rs.getString("title"),
                rs.getString("file_path"),
                rs.getString("thumbnail_path")
        );
    }
}
