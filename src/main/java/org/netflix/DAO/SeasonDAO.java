package org.netflix.DAO;

import org.netflix.Models.Season;
import org.netflix.Utils.ConxDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeasonDAO {
    private static Connection conn = ConxDB.getInstance();

    public List<Season> getSeasonsBySerie(int idSerie) {
        List<Season> seasons = new ArrayList<>();
        // Fixed: Ensure the query matches your schema table 'saison'
        String sql = "SELECT id_Saison, id_Serie, saisonNumber, title, description FROM saison WHERE id_Serie = ? ORDER BY saisonNumber ASC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idSerie);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Added: Basic null handling to prevent UI crashes
                    String title = rs.getString("title") != null ? rs.getString("title") : "Season " + rs.getInt("saisonNumber");
                    String desc = rs.getString("description") != null ? rs.getString("description") : "No description available.";

                    seasons.add(new Season(
                            rs.getInt("id_Saison"),
                            rs.getInt("id_Serie"),
                            rs.getInt("saisonNumber"),
                            title,
                            desc
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching seasons for Serie ID " + idSerie + ": " + e.getMessage());
        }
        return seasons;
    }

    public static boolean deleteSeason(int idSaison) {
        String sql = "DELETE FROM saison WHERE id_Saison = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idSaison);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addSeason(Season season) {
        // Double check: In your schema, id_Saison looks like an Auto-Increment PK.
        // We don't include it in the INSERT.
        String sql = "INSERT INTO saison (id_Serie, saisonNumber, title, description) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, season.getIdSerie());
            pstmt.setInt(2, season.getSeasonNumber());
            pstmt.setString(3, season.getTitle());
            pstmt.setString(4, season.getDescription());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}