package org.netflix.DAO;
import org.netflix.Models.Rating;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.*;
import java.util.Optional;

public class RatingDAO {
    private static Connection conn = ConxDB.getInstance();
    public static boolean saveOrUpdateRating(Rating rating) {
        // On utilise 'ON DUPLICATE KEY UPDATE' pour permettre à l'utilisateur de changer sa note
        String sql = "INSERT INTO rating (id_User, id_Media, score, ratingDate) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE score = ?, ratingDate = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rating.getId_User());
            pstmt.setInt(2, rating.getId_Media());
            pstmt.setFloat(3, rating.getScore());
            pstmt.setDate(4, Date.valueOf(rating.getRatingDate()));

            // Pour l'update
            pstmt.setFloat(5, rating.getScore());
            pstmt.setDate(6, Date.valueOf(rating.getRatingDate()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static double getAverageRating(int id_Media) {
        String sql = "SELECT AVG(score) as average FROM rating WHERE id_Media = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_Media);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("average");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    public static Rating getUserRating(int id_User, int id_Media) {
        String sql = "SELECT * FROM rating WHERE id_User = ? AND id_Media = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_User);
            pstmt.setInt(2, id_Media);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Rating(
                            rs.getInt("id_Rating"),
                            rs.getInt("id_User"),
                            rs.getInt("id_Media"),
                            rs.getFloat("score"),
                            rs.getTimestamp("ratingDate").toLocalDateTime().toLocalDate()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static List<Integer> getTopRatedMediaIds(int limit) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id_Media, AVG(score) as avg_score FROM rating GROUP BY id_Media ORDER BY avg_score DESC LIMIT ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id_Media"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

        public boolean insert(Rating rating) {
            String sql = "INSERT INTO ratings (user_id, media_id, rating, rated_at) VALUES (?, ?, ?, ?)";

            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setInt(1, rating.getUserId());
                pstmt.setInt(2, rating.getMediaId());
                pstmt.setInt(3, rating.getRating());
                pstmt.setTimestamp(4, rating.getRatedAt());

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            rating.setId(generatedKeys.getInt(1));
                            return true;
                        }
                    }
                }
                return false;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean update(Rating rating) {
            String sql = "UPDATE ratings SET rating = ?, rated_at = ? WHERE id = ?";

            try (Connection conn =ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, rating.getRating());
                pstmt.setTimestamp(2, rating.getRatedAt());
                pstmt.setInt(3, rating.getId());

                return pstmt.executeUpdate() > 0;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean delete(int id) {
            String sql = "DELETE FROM ratings WHERE id = ?";

            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }


        public Optional<Rating> findByUserAndMedia(int userId, int mediaId) {
            String sql = "SELECT * FROM ratings WHERE user_id = ? AND media_id = ?";

            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, userId);
                pstmt.setInt(2, mediaId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToRating(rs));
                    }
                }
                return Optional.empty();

            } catch (SQLException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        }

        public List<Rating> findByUser(int userId) {
            String sql = "SELECT * FROM ratings WHERE user_id = ? ORDER BY rated_at DESC";
            List<Rating> ratings = new ArrayList<>();

            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, userId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        ratings.add(mapResultSetToRating(rs));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return ratings;
        }

        public List<Rating> findByMedia(int mediaId) {
            String sql = "SELECT * FROM ratings WHERE media_id = ? ORDER BY rated_at DESC";
            List<Rating> ratings = new ArrayList<>();

            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, mediaId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        ratings.add(mapResultSetToRating(rs));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return ratings;
        }

        public List<Rating> findRecentRatings(int limit) {
            String sql = "SELECT * FROM ratings ORDER BY rated_at DESC LIMIT ?";
            List<Rating> ratings = new ArrayList<>();

            try (Connection conn =ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, limit);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        ratings.add(mapResultSetToRating(rs));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return ratings;
        }


        public double calculateAverageRating(int mediaId) {
            String sql = "SELECT AVG(rating) as avg_rating FROM ratings WHERE media_id = ?";

            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, mediaId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("avg_rating");
                    }
                }
                return 0.0;

            } catch (SQLException e) {
                e.printStackTrace();
                return 0.0;
            }
        }

        public int countByMedia(int mediaId) {
            String sql = "SELECT COUNT(*) FROM ratings WHERE media_id = ?";

            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, mediaId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
                return 0;

            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }
        }

        public Map<Integer, Integer> getRatingDistribution(int mediaId) {
            String sql = "SELECT rating, COUNT(*) as count FROM ratings WHERE media_id = ? GROUP BY rating ORDER BY rating";
            Map<Integer, Integer> distribution = new HashMap<>();

            try (Connection conn =ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, mediaId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        distribution.put(rs.getInt("rating"), rs.getInt("count"));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Initialiser les notes manquantes à 0
            for (int i = 1; i <= 5; i++) {
                distribution.putIfAbsent(i, 0);
            }

            return distribution;
        }


        public List<TopRatedMedia> getTopRatedMedia(int limit) {
            String sql = "SELECT media_id, AVG(rating) as avg_rating, COUNT(*) as rating_count " +
                    "FROM ratings GROUP BY media_id ORDER BY avg_rating DESC LIMIT ?";
            List<TopRatedMedia> topRated = new ArrayList<>();

            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, limit);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int mediaId = rs.getInt("media_id");
                        double avgRating = rs.getDouble("avg_rating");
                        int ratingCount = rs.getInt("rating_count");

                        String title = getMediaTitle(mediaId);
                        String type = getMediaType(mediaId);
                        String cover = getMediaCover(mediaId);

                        topRated.add(new TopRatedMedia(mediaId, title, type, cover, avgRating, ratingCount));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return topRated;
        }

        public List<TopRatedMedia> getTopRatedMovies(int limit) {
            String sql = "SELECT r.media_id, AVG(r.rating) as avg_rating, COUNT(*) as rating_count " +
                    "FROM ratings r " +
                    "JOIN movies m ON r.media_id = m.id " +
                    "GROUP BY r.media_id ORDER BY avg_rating DESC LIMIT ?";
            List<TopRatedMedia> topRated = new ArrayList<>();

            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, limit);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int mediaId = rs.getInt("media_id");
                        double avgRating = rs.getDouble("avg_rating");
                        int ratingCount = rs.getInt("rating_count");

                        String title = getMediaTitle(mediaId);
                        String cover = getMediaCover(mediaId);

                        topRated.add(new TopRatedMedia(mediaId, title, "MOVIE", cover, avgRating, ratingCount));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return topRated;
        }

        public List<TopRatedMedia> getTopRatedSeries(int limit) {
            String sql = "SELECT r.media_id, AVG(r.rating) as avg_rating, COUNT(*) as rating_count " +
                    "FROM ratings r " +
                    "JOIN series s ON r.media_id = s.id " +
                    "GROUP BY r.media_id ORDER BY avg_rating DESC LIMIT ?";
            List<TopRatedMedia> topRated = new ArrayList<>();

            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, limit);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int mediaId = rs.getInt("media_id");
                        double avgRating = rs.getDouble("avg_rating");
                        int ratingCount = rs.getInt("rating_count");

                        String title = getMediaTitle(mediaId);
                        String cover = getMediaCover(mediaId);

                        topRated.add(new TopRatedMedia(mediaId, title, "SERIE", cover, avgRating, ratingCount));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return topRated;
        }


        private Rating mapResultSetToRating(ResultSet rs) throws SQLException {
            return new Rating(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("media_id"),
                    rs.getInt("rating"),
                    rs.getTimestamp("rated_at")
            );
        }

        private String getMediaTitle(int mediaId) {
            // Vérifier si c'est un film
            String sql = "SELECT title FROM movies WHERE id = ?";
            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, mediaId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("title");
                    }
                }
            } catch (SQLException e) {
                // Ignorer
            }

            // Vérifier si c'est une série
            sql = "SELECT title FROM series WHERE id = ?";
            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, mediaId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("title");
                    }
                }
            } catch (SQLException e) {
                // Ignorer
            }

            return "Média inconnu";
        }

        private String getMediaType(int mediaId) {
            try (Connection conn = ConxDB.getConnection()) {
                String sql = "SELECT 'MOVIE' as type FROM movies WHERE id = ? UNION SELECT 'SERIE' as type FROM series WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, mediaId);
                    pstmt.setInt(2, mediaId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getString("type");
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return "UNKNOWN";
        }

        private String getMediaCover(int mediaId) {

            String sql = "SELECT cover_image_url FROM movies WHERE id = ?";
            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, mediaId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("cover_image_url");
                    }
                }
            } catch (SQLException e) {
                // Ignorer
            }


            sql = "SELECT cover_image_url FROM series WHERE id = ?";
            try (Connection conn = ConxDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, mediaId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("cover_image_url");
                    }
                }
            } catch (SQLException e) {
                // Ignorer
            }

            return "";
        }
    }
}
