package org.netflix.DAO;

import org.netflix.Models.Movie;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {
    private static Connection conn = ConxDB.getInstance();

    public static List<Movie> getAllMovies(){
        List<Movie> movies = new ArrayList<Movie>();
        String sql = "SELECT m.*, v.videoUrl, v.duration_minutes " +
                "FROM media m " +
                "INNER JOIN movie v ON m.id_Media = v.id_Media";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            try {
                while (rs.next()) {
                    movies.add(new Movie(
                            rs.getInt("id_Media"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("releaseYear"),
                            rs.getDouble("averageRating"),
                            rs.getString("coverImageUrl"),
                            rs.getString("director"),
                            rs.getString("videoUrl"),
                            rs.getInt("duration_minutes"),
                            rs.getString("type")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  movies;
    }
    public void addMovie(Movie movie){
        String sqlMedia="INSERT INTO media (title, description,releaseYear, averageRating,coverImageUrl,director,type) VALUES(?,?,?,?,?,?,'movie')";
        String sqlMovie="INSERT INTO movie(id_Media,videoUrl,duration_minutes) VALUES(?,?,?)";
        try{
            conn.setAutoCommit(false);
            try(PreparedStatement pstmtMedia=conn.prepareStatement(sqlMedia,Statement.RETURN_GENERATED_KEYS)){
                pstmtMedia.setString(1, movie.getTitle());
                pstmtMedia.setString(2, movie.getDescription());
                pstmtMedia.setInt(3, movie.getReleaseYear());
                pstmtMedia.setDouble(4, movie.getAverageRating());
                pstmtMedia.setString(5, movie.getCoverImageUrl());
                pstmtMedia.setString(6, movie.getDirector());
                pstmtMedia.executeUpdate();
                ResultSet rs=pstmtMedia.getGeneratedKeys();
                if(rs.next()){
                    int generatedId=rs.getInt(1);
                    try (PreparedStatement pstmtMovie= conn.prepareStatement(sqlMovie)){
                        pstmtMovie.setInt(1,generatedId);
                        pstmtMovie.setString(2, movie.getVideoUrl());
                        pstmtMovie.setInt(3, movie.getDurationMinutes());
                        pstmtMovie.executeUpdate();
                    }
                }
            }
            conn.commit();
        }catch(SQLException e){
            try{conn.rollback();}catch(SQLException ex){ex.printStackTrace();}
            e.printStackTrace();
        }}
        public void deleteMovie(int idMedia) {
            String sql = "DELETE FROM media WHERE id_Media = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);) {
                pstmt.setInt(1, idMedia);
                pstmt.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }

}
