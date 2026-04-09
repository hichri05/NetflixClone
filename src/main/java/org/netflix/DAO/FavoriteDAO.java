package org.netflix.DAO;

import org.netflix.Models.Media;
import java.util.List;

public class FavoriteDAO {

    public boolean insert(int userId, int mediaId) {
        String sql = "INSERT INTO favorites (id_user, id_media) VALUES (?,?)";
        return false;
    }

    public boolean delete(int userId, int mediaId) {
        String sql = "DELETE FROM favorites WHERE id_user=? AND id_media=?";
        return false;
    }

    public boolean exists(int userId, int mediaId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE id_user=? AND id_media=?";
        return false;
    }

    public List<Media> findByUser(int userId) {
        String sql = """
            SELECT m.* FROM media m
            JOIN favorites f ON m.id = f.id_media
            WHERE f.id_user = ?
        """;
        return null;
    }
}