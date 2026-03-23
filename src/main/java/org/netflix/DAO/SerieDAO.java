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

}
