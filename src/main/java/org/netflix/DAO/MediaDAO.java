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
                    medias.add(new Media(
                            rs.getInt("id_Media"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("releaseYear"),
                            rs.getDouble("averageRating"),
                            rs.getString("coverImageUrl"),
                            rs.getString("backdrop_path"),
                            rs.getString("director")
                    ));
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
}
