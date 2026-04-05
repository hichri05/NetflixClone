package org.netflix.DAO;
import org.netflix.Models.Season;
import org.netflix.Utils.ConxDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeasonDAO {
    private static Connection conn = ConxDB.getInstance();

    public List<Season> getSeasonsBySerie(int idSerie) {
        List<Season> seasons = new ArrayList<>();
        String sql = "SELECT * FROM saison WHERE id_Serie = ? ORDER BY saisonNumber ASC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idSerie);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    seasons.add(new Season(
                            rs.getInt("id_Saison"),
                            rs.getInt("id_Serie"),
                            rs.getInt("saisonNumber"),
                            rs.getString("title"),
                            rs.getString("description")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seasons;
    }
    public static boolean deleteSeason(int idSaison) {
        String sql = "DELETE FROM saison WHERE id_Saison = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idSaison);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la saison : " + e.getMessage());
            return false;
        }
    }
    public static boolean addSeason(Season season) {
        String sql = "INSERT INTO saison (id_Serie, saisonNumber, title, description) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, season.getIdSerie());
            pstmt.setInt(2, season.getSeasonNumber());
            pstmt.setString(3, season.getTitle());
            pstmt.setString(4, season.getDescription());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la saison : " + e.getMessage());
            return false;
        }
    }
    public boolean update(Season season) {
        String sql = "UPDATE seasons SET season_number = ?, title = ?, description = ? WHERE id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, season.getSeasonNumber());
            pstmt.setString(2, season.getTitle());
            pstmt.setString(3, season.getDescription());
            pstmt.setInt(4, season.getIdSeason());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public Optional<Season> findById(int id) {
        String sql = "SELECT * FROM seasons WHERE id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSeason(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<Season> findBySerieIdAndNumber(int serieId, int seasonNumber) {
        String sql = "SELECT * FROM seasons WHERE serie_id = ? AND season_number = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, serieId);
            pstmt.setInt(2, seasonNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSeason(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public int countBySerieId(int serieId) {
        String sql = "SELECT COUNT(*) FROM seasons WHERE serie_id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, serieId);

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
    private Season mapResultSetToSeason(ResultSet rs) throws SQLException {
        return new Season(
                rs.getInt("id"),
                rs.getInt("serie_id"),
                rs.getInt("season_number"),
                rs.getString("title"),
                rs.getString("description")
        );
    }
}
}
