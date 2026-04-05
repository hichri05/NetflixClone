package org.netflix.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

import static com.mysql.cj.conf.PropertyKey.PASSWORD;
import static javafx.scene.input.DataFormat.URL;

public class ConxDB {
    private static Connection connexion;
    Properties prop = new Properties();
    private final String DB_URL = "jdbc:mysql://localhost:3306/bingepanda";
    private final String USER = "root";
    private final String PASS = "";

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
    } public static Connection getInstance() {
        if(connexion == null)
            try {
                new ConxDB();
            }catch (Exception e) {
                System.out.println("--"+ e.getMessage());
            }
        return connexion;
    }
        public static Connection getConnection() throws SQLException {
            try {
                Connection conn = DriverManager.getConnection(
                        URL, USER, PASSWORD);
                System.out.println("Connexion à la base de données établie avec succès");
                return conn;
            } catch (SQLException e) {
                System.err.println("Erreur de connexion à la base de données");
                System.err.println("URL: " + URL);
                System.err.println("Message: " + e.getMessage());
                throw e;
            }
        }

        /**
         * Teste la connexion à la base de données
         * @return true si la connexion réussit, false sinon
         */
        public static boolean testConnection() {
            try (Connection conn = getConnection()) {
                return conn != null && !conn.isClosed();
            } catch (SQLException e) {
                System.err.println("Test de connexion échoué: " + e.getMessage());
                return false;
            }
        }

        /**
         * Ferme une connexion proprement
         * @param conn La connexion à fermer
         */
        public static void closeConnection(Connection conn) {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("Connexion fermée avec succès");
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
                }
            }
        }
    }

