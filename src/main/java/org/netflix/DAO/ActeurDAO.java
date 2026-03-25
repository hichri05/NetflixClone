package org.netflix.DAO;
import org.netflix.Models.Acteur;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActeurDAO {
    private static Connection conn = ConxDB.getInstance();
    public static boolean addActeur(Acteur acteur) {
        String sql = "INSERT INTO acteur (nom, age, acteurImageUrl) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, acteur.getNom());
            pstmt.setInt(2, acteur.getAge());
            pstmt.setString(3, acteur.getActeurImageUrl());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static List<Acteur> getActeursByMedia(int id_Media) {
        List<Acteur> acteurs = new ArrayList<>();
        // On suppose l'existence d'une table de liaison 'media_acteur'
        String sql = "SELECT a.* FROM acteur a " +
                "JOIN media_acteur ma ON a.id_Acteur = ma.id_Acteur " +
                "WHERE ma.id_Media = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_Media);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    acteurs.add(new Acteur(
                            rs.getInt("id_Acteur"),
                            rs.getString("nom"),
                            rs.getInt("age"),
                            rs.getString("acteurImageUrl")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return acteurs;
    }


    public static List<Acteur> getAllActeurs() {
        List<Acteur> acteurs = new ArrayList<>();
        String sql = "SELECT * FROM acteur";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                acteurs.add(new Acteur(
                        rs.getInt("id_Acteur"),
                        rs.getString("nom"),
                        rs.getInt("age"),
                        rs.getString("acteurImageUrl")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return acteurs;
    }


    public static boolean deleteActeur(int id_Acteur) {
        String sql = "DELETE FROM acteur WHERE id_Acteur = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_Acteur);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
