package org.netflix.DAO;



import org.netflix.Models.Payment;
import org.netflix.Utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    private static Connection conn = ConxDB.getInstance();

    public static boolean addPayment(Payment payment) {
        String sql = "INSERT INTO payment (id_User, id_Subscription, amount, currency, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, payment.getId_User());
            pstmt.setInt(2, payment.getId_Subscription());
            pstmt.setFloat(3, payment.getAmount());
            pstmt.setString(4, payment.getCurrency());
            pstmt.setInt(5, payment.getStatus());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static List<Payment> getPaymentsByUser(int idUser) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payment WHERE id_User = ? ORDER BY id_payment DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUser);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(new Payment(
                            rs.getInt("id_payment"),
                            rs.getInt("id_User"),
                            rs.getInt("id_Subscription"),
                            rs.getFloat("amount"),
                            rs.getString("currency"),
                            rs.getInt("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }


    public static boolean updatePaymentStatus(int idPayment, int newStatus) {
        String sql = "UPDATE payment SET status = ? WHERE id_payment = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newStatus);
            pstmt.setInt(2, idPayment);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static double getTotalRevenue() {
        String sql = "SELECT SUM(amount) as total FROM payment WHERE status = 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
