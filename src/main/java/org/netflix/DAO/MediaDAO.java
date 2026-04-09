package org.netflix.DAO;

import org.netflix.Models.*;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MediaDAO {
    private static Connection conn = ConxDB.getInstance();

    public static List<Genre> getGenresByMediaId(int mediaId) {
        List<Genre> genres = new ArrayList<>();
        String sql = "SELECT g.* FROM genres g " +
                "inner join media_genres mg on mg.id_Genre=g.id_Genre " +
                "WHERE mg.id_Media = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mediaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    genres.add(new Genre(rs.getInt("id_Genre"),
                            MediaGenre.fromString(rs.getString("name"))
                    ));
                }
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
                "where mg.id_Genre = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, genreId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Media m = ResultToMedia(rs);
                    // On charge les genres APRES avoir fini avec le ResultSet si nécessaire
                    // ou on utilise une méthode qui ne boucle pas sur la connexion
                    medias.add(m);
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }

        // Charger les genres pour chaque média pour éviter la récursion SQL
        for(Media m : medias) {
            m.setGenres(getGenresByMediaId(m.getIdMedia()));
        }

        return medias;
    }

    public static List<Media> getTrendingMedias() {
        return new ArrayList<>();
    }

    public static int getGenreIdByName(String genreName) {
        String sql = "select id_Genre from genres where name = ?";
        int genreId = -1;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, genreName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    genreId = rs.getInt("id_Genre");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching genres " + genreName);
        }
        return genreId;
    }

    public static List<Media> searchMedia(String search) {
        List<Media> results = new ArrayList<>();
        String sql = "SELECT * FROM media WHERE LOWER(title) LIKE LOWER(?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + search + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(ResultToMedia(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for(Media m : results) {
            m.setGenres(getGenresByMediaId(m.getIdMedia()));
        }
        return results;
    }

    // Version allégée pour éviter le StackOverflow
    static Media ResultToMedia(ResultSet rs) throws SQLException {
        return new Media(
                rs.getInt("id_Media"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("releaseYear"),
                rs.getDouble("averageRating"),
                rs.getString("coverImageUrl"),
                rs.getString("backdrop_path"),
                rs.getString("director"),
                new ArrayList<>(),          // genres (loaded separately to avoid recursion)
                new ArrayList<>(),          // casting
                rs.getInt("views"),         // ← views
                rs.getString("type")        // ← type LAST
        );
    }
    public static List<Media> getAllMediaWithViews() {
        List<Media> mediaList = new ArrayList<>();
        String sql = "SELECT id_Media, title, description, releaseYear, averageRating, " +
                "coverImageUrl, backdrop_path, director, type, views FROM media";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_Media");
                List<Genre> genres = getGenresByMediaId(id);

                mediaList.add(new Media(
                        id,
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("releaseYear"),
                        rs.getDouble("averageRating"),
                        rs.getString("coverImageUrl"),
                        rs.getString("backdrop_path"),  // backdropImageUrl
                        rs.getString("director"),
                        genres,                          // ← genres before casting
                        new ArrayList<>(),               // casting
                        rs.getInt("views"),              // ← views
                        rs.getString("type")             // ← type LAST, not duplicated
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error in getAllMediaWithViews: " + e.getMessage());
            e.printStackTrace();
        }
        return mediaList;
    }
    public static boolean addMedia(Media media) {
        String sql = "INSERT INTO media (title, description, releaseYear, averageRating, coverImageUrl, director, type, views) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 0)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, media.getTitle());
            pstmt.setString(2, media.getDescription());
            pstmt.setInt(3, media.getReleaseYear());
            pstmt.setDouble(4, media.getAverageRating());
            pstmt.setString(5, media.getCoverImageUrl());
            pstmt.setString(6, media.getDirector());
            pstmt.setString(7, media.getType());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
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
            System.err.println("Erreur lors de la mise à jour : " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteMedia(int idMedia) {
        String sql = "DELETE FROM media WHERE id_Media = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idMedia);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            return false;
        }
    }

    public static boolean addToFavorites(int id, int idMedia) {
        String sql = "INSERT INTO favorite (id_User, id_Media) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, idMedia);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void removeFromFavorites(int id, int idMedia) {
        String sql = "DELETE FROM favorite WHERE id_user = ? AND id_media = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, idMedia);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Media> getAllMedia() {
        List<Media> mediaList = new ArrayList<>();
        String sql = "SELECT * FROM media";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                mediaList.add(ResultToMedia(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for(Media m : mediaList) {
            m.setGenres(getGenresByMediaId(m.getIdMedia()));
        }
        return mediaList;
    }
    public static List<Media> getTopViews() {
        List<Media> medias = new ArrayList<>();
        String sql = "SELECT * FROM media ORDER BY views DESC LIMIT 10";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {

                medias.add(ResultToMedia(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return medias;
    }
    // ✅ NOUVEAUX AJOUTS dans MediaDAO

    public List<Media> searchByKeyword(String keyword) {
        String sql = "SELECT * FROM media WHERE title LIKE ? OR description LIKE ?";
        // paramètre : "%" + keyword + "%"
        return null;
    }

    public List<Media> findByGenre(String genre) {
        String sql = """
        SELECT DISTINCT m.* FROM media m
        JOIN media_genre mg ON m.id = mg.id_media
        JOIN genre g ON mg.id_genre = g.id
        WHERE g.name = ?
    """;
        return null;
    }

    public List<Media> findByYear(int year) {
        String sql = "SELECT * FROM media WHERE release_year = ?";
        return null;
    }

    public List<Media> findFeatured() {
        String sql = "SELECT * FROM media ORDER BY release_year DESC, average_rating DESC LIMIT 5";
        return null;
    }

    public List<Media> findTop5ByViews() {
        String sql = "SELECT * FROM media ORDER BY views DESC LIMIT 5";
        return null;
    }

    public Map<String, Long> countByGenre() {
        String sql = """
        SELECT g.name, COUNT(*) as total FROM media m
        JOIN media_genre mg ON m.id = mg.id_media
        JOIN genre g ON mg.id_genre = g.id
        GROUP BY g.name
    """;
        // Remplir une Map<String, Long>
        return null;
    }

    public void updateAverageRating(int mediaId, double avg) {
        String sql = "UPDATE media SET average_rating = ? WHERE id = ?";
        // ... JDBC ...
    }

    public boolean insertMovie(Movie movie) {
        // INSERT dans media + movie
        return false;
    }

    public boolean insertSerie(Serie serie) {
        // INSERT dans media + serie
        return false;
    }

    public List<Movie> findAllMovies() {
        String sql = "SELECT * FROM media WHERE type = 'MOVIE'";
        return null;
    }

    public List<Serie> findAllSeries() {
        String sql = "SELECT * FROM media WHERE type = 'SERIE'";
        return null;
    }
}