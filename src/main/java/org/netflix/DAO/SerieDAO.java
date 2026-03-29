package org.netflix.DAO;
import org.netflix.Models.Genre;
import org.netflix.Models.Serie;
import org.netflix.Utils.ConxDB;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SerieDAO {
    private static Connection  con=ConxDB.getInstance();
    public List <Serie> getAllSeries(){
        List<Serie> series =new ArrayList<>();
        //ha4i l requettee
        String sql="SELECT m.*, s.nbrSaison"+
                "FROM media m"+
                "INNER JOIN serie s ON m.id_Media=s.id_Media" ;
        try(Statement stmt=con.createStatement(); ResultSet rs =stmt.executeQuery(sql))
        {
            while(rs.next()){
                List<Genre> genresList = MediaDAO.getGenresByMediaId(rs.getInt("id_Media"));
                series.add(new Serie(rs.getInt("id_Media"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("releaseYear"),
                        rs.getDouble("averageRating"),
                        rs.getString("coverImageUrl"),
                        rs.getString("director"),
                        rs.getInt("nbrSaison"),
                        genresList
                                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return series;
    }
    // Dans SerieDAO.java - Ajouter cette méthode
    public boolean updateRating(Serie serie) {
        String sql = "UPDATE series SET average_rating = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, serie.getAverageRating());
            pstmt.setInt(2, serie.getIdMedia());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Dans SerieDAO.java - Ajouter cette méthode
    public List<Serie> findTopRated(int limit) {
        String sql = "SELECT * FROM series ORDER BY average_rating DESC LIMIT ?";
        List<Serie> series = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    series.add(mapResultSetToSerie(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return series;
    }
    // Dans SerieDAO.java - Ajouter cette méthode

    public boolean updateSeasonCount(int serieId, int seasonCount) {
        String sql = "UPDATE series SET number_of_seasons = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, seasonCount);
            pstmt.setInt(2, serieId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
