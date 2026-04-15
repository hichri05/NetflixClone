package org.netflix.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

public class ConxDB {
    private static Connection connexion;
    Properties prop = new Properties();
    private final String DB_URL = "jdbc:mysql://localhost:3306/netflix";
    private final String USER = "root";
    private final String PASS = "";

    private ConxDB() throws SQLException{
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        try (InputStream input = ConxDB.class.getClassLoader().getResourceAsStream("config.properties")) {

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
            }
            prop.load(input);
            String port = prop.getProperty("db.port");
            String url = "jdbc:mysql://localhost:" + port + "/netflix";

            connexion =  DriverManager.getConnection(url, prop.getProperty("db.user"), prop.getProperty("db.pass"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    /*
    private ConxDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Test direct avec le port 3307
            String url = "jdbc:mysql://localhost:3307/netflix?serverTimezone=UTC";
            connexion = DriverManager.getConnection(url, "root", "");
            System.out.println("Connexion forcée réussie !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public static Connection getInstance() {
        if(connexion == null)
            try {
                new ConxDB();
            }catch (Exception e) {
                System.out.println("--"+ e.getMessage());
            }
        return connexion;
    }
    public static Connection getConnection() throws SQLException {
        String URL      = "jdbc:mysql://localhost:3306/netflix";
        String USER     = "root";
        String PASSWORD = "";

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

}