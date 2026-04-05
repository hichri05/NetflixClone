// package org.netflix.DAO;

package org.netflix.DAO;

import org.netflix.Models.Genre;
import org.netflix.Utils.ConxDB;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenreDAO {

    public List<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY name";
        List<Genre> genres = new ArrayList<>();

        try (Connection conn =ConxDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                genres.add(mapResultSetToGenre(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }

    public Optional<Genre> findById(int id) {
        String sql = "SELECT * FROM genres WHERE id = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGenre(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<Genre> findByName(String name) {
        String sql = "SELECT * FROM genres WHERE name = ?";

        try (Connection conn = ConxDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGenre(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private Genre mapResultSetToGenre(ResultSet rs) throws SQLException {
        return new Genre(
                rs.getInt("id"),
                rs.getString("name")
        );
    }
}