package org.netflix.DAO;
import org.netflix.Models.Acteur;
import org.netflix.Models.Genre;
import org.netflix.Models.Serie;
import org.netflix.Utils.ConxDB;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SerieDAO {
    private static Connection  con=ConxDB.getInstance();
    public List <Serie> getAllSeries(){
        List<Serie> series =new ArrayList<>();
        List<Acteur> casting=new ArrayList<>();
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
                        rs.getString("backdrop_path"),
                        rs.getString("director"),
                        rs.getInt("nbrSaison"),
                        genresList,
                        casting
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

        try (Connection conn = ConxDB.getConnection();
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

        try (Connection conn = ConxDB.getConnection();
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

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, seasonCount);
            pstmt.setInt(2, serieId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public Optional<Serie> findById(int id) {
        String sql = "SELECT * FROM series WHERE id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSerie(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public List<Serie> findByGenre(int genreId) {
        String sql = "SELECT s.* FROM series s " +
                "JOIN media_genres mg ON s.id = mg.media_id " +
                "WHERE mg.genre_id = ? AND mg.media_type = 'SERIE'";
        List<Serie> series = new ArrayList<>();

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, genreId);

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

    public List<Serie> search(String keyword) {
        String sql = "SELECT * FROM series WHERE " +
                "title LIKE ? OR description LIKE ? OR director LIKE ?";
        List<Serie> series = new ArrayList<>();

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

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

    public int count() {
        String sql = "SELECT COUNT(*) FROM series";

        try (Connection conn = ConxDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Serie mapResultSetToSerie(ResultSet rs) throws SQLException {
        return new Serie(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("release_year"),
                rs.getDouble("average_rating"),
                rs.getString("cover_image_url"),
                rs.getString("director"),
                rs.getInt("number_of_seasons"),
                "SERIE"
        );
    }
    // Dans SerieDAO.java - Ajouter cette méthode
    public boolean updateRating(Serie serie) {
        String sql = "UPDATE series SET average_rating = ? WHERE id = ?";

        try (Connection conn = ConxDB.getConnection();
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

        try (Connection conn = ConxDB.getConnection();
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
}

