package org.netflix.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConxDB {
    private static Connection connexion;
    private final String DB_URL = "jdbc:mysql://localhost:3306/netflix";
    private final String USER = "root";
    private final String PASS = "";

    private ConxDB() throws SQLException{
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        connexion = DriverManager.getConnection(DB_URL, USER, PASS);
    }
    public static Connection getInstance() {
        if(connexion == null)
            try {
                new ConxDB();
            }catch (Exception e) {
                System.out.println("--"+ e.getMessage());
            }
        return connexion;
    }

}
