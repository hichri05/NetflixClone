package org.netflix.DAO;

import org.netflix.Models.User;
import org.netflix.Utils.ConxDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static Connection conn = ConxDB.getInstance();

    public static List<User> getAllUsers()
    {
        Statement stmt = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<User>();
        String SQL = "SELECT * FROM users";
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(SQL);

            while (rs.next()){
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String Email = rs.getString("email");

                User user = new User(id, username, Email);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }  finally {
            try {
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return users;
    }
}
