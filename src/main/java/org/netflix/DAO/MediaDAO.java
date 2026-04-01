package org.netflix.DAO;

import org.netflix.Models.Genre;
import org.netflix.Models.Media;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public static List<Media> getMediasByGenre(String genre) {
        List<Media> medias = new ArrayList<>();
        int genreId = getGenreIdByName(genre);
        String sql = "SELECT m.* FROM Media m " +
                "inner join media_genres mg on m.id_Media = mg.id_Media " +
                "where mg.id_Genre = ?" ;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, genreId);
            try (ResultSet rs = pstmt.executeQuery()){
                while (rs.next()) {
                    medias.add(ResultToMedia(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println(e);
        }
        return medias;
    }

    public static List<Media> getTrendingMedias() {
        //todo
        return null;
    }

    public static int getGenreIdByName(String genreName) {
        String sql = "select id_Genre from genres where name = ?" ;
        int genreId = -1;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, genreName);
            try(ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    genreId = rs.getInt("id_Genre");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching genres" + genreName);
        }
        return genreId;
    }
    public static List<Media> searchMedia(String search) {
        List<Media> results = new ArrayList<>();

        String sql = "SELECT * FROM media WHERE LOWER(title) LIKE LOWER(?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + search + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(ResultToMedia(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    static Media ResultToMedia(ResultSet rs) throws SQLException {
        return new Media(
                rs.getInt("id_Media"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("releaseYear"),
                rs.getDouble("averageRating"),
                rs.getString("coverImageUrl"),
                rs.getString("backdrop_path"),
                rs.getString("director")
        );
    }
    /**
     * Ajout
     */
    public static boolean addMedia(Media media, String type) {
        String sql = "INSERT INTO media (title, description, releaseYear, averageRating, coverImageUrl, director, type, views) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 0)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, media.getTitle());
            pstmt.setString(2, media.getDescription());
            pstmt.setInt(3, media.getReleaseYear());
            pstmt.setDouble(4, media.getAverageRating());
            pstmt.setString(5, media.getCoverImageUrl());
            pstmt.setString(6, media.getDirector());
            pstmt.setString(7, type); // 'movie' ou 'serie'

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Récupération de l'ID généré pour l'utiliser dans MovieDAO ou SerieDAO si besoin
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        media.setIdMedia(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du média : " + e.getMessage());
        }
        return false;
    }

    /**
     * ha4i l update
     */
    public static boolean updateMedia(Media media) {
        String sql = "UPDATE media SET title = ?, description = ?, releaseYear = ?, " +
                "averageRating = ?, coverImageUrl = ?, director = ? WHERE id_Media = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, media.getTitle());
            pstmt.setString(2, media.getDescription());
            pstmt.setInt(3, media.getReleaseYear());
            pstmt.setDouble(4, media.getAverageRating());
            pstmt.setString(5, media.getCoverImageUrl());
            pstmt.setString(6, media.getDirector());
            pstmt.setInt(7, media.getIdMedia());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du média : " + e.getMessage());
            return false;
        }
    }

    /**
     * supprission
     */
    public static boolean deleteMedia(int idMedia) {
        // Note : Grâce aux contraintes ON DELETE CASCADE en SQL,
        // supprimer ici supprimera aussi les entrées dans movie/serie/favorite/media_genre
        String sql = "DELETE FROM media WHERE id_Media = ? ON DELETE CASCADE";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idMedia);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            return false;
        }
    }

    public static boolean addToFavorites(int id, int idMedia) {
        return true;
    }

    public static void removeFromFavorites(int id, int idMedia) {
    }

    public static Media getAllMedia() {
        //todo
        return null;
    }
}


