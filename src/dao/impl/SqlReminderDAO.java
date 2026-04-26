package dao.impl;

import dao.IReminderDAO;
import dao.helper.DBConnection;
import model.Reminder;
import model.ReminderType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlReminderDAO implements IReminderDAO {

    // --- CÁC HÀM XỬ LÝ THEO NGHIỆP VỤ (CHÍNH) ---

    @Override
    public void insertReminders(String appId, List<Reminder> reminders) {
        // Đã xóa isSent khỏi câu lệnh SQL
        String sql = "INSERT INTO Reminder (appId, type, minutesBefore, message) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // Bắt đầu Transaction

            for (Reminder rem : reminders) {
                pstmt.setString(1, appId);
                pstmt.setString(2, rem.getType().name()); // Lưu dưới dạng String (POPUP, EMAIL)
                pstmt.setInt(3, rem.getMinutesBefore());
                pstmt.setString(4, rem.getMessage());
                pstmt.addBatch(); // Nạp vào hàng chờ
            }

            pstmt.executeBatch();
            conn.commit(); // Xác nhận Transaction

            System.out.println("DAO: Đã lưu thành công " + reminders.size() + " reminders cho Lịch: " + appId);
        } catch (SQLException e) {
            System.err.println("Lỗi insertReminders SQL: " + e.getMessage());
        }
    }

    @Override
    public void deleteRemindersByAppId(String appId) {
        String sql = "DELETE FROM Reminder WHERE appId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, appId);
            int rowsDeleted = pstmt.executeUpdate();
            System.out.println("DAO: Đã dọn dẹp " + rowsDeleted + " reminders cũ của Lịch: " + appId);

        } catch (SQLException e) {
            System.err.println("Lỗi deleteRemindersByAppId SQL: " + e.getMessage());
        }
    }

    @Override
    public List<Reminder> getRemindersByAppId(String appId) {
        List<Reminder> list = new ArrayList<>();
        String sql = "SELECT * FROM Reminder WHERE appId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, appId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Đã xóa isSent
                Reminder rem = new Reminder(
                        ReminderType.valueOf(rs.getString("type")),
                        rs.getInt("minutesBefore"),
                        rs.getString("message")
                );
                list.add(rem);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getRemindersByAppId SQL: " + e.getMessage());
        }
        return list;
    }


    // --- CÁC HÀM OVERRIDE TỪ IBASEDAO ---

    @Override
    public String insert(Reminder entity) {
        // Vì Reminder không có appId bên trong nó, việc insert đơn lẻ sẽ lỗi Foreign Key.
        // Ép buộc lập trình viên phải dùng hàm insertReminders(appId, list) ở trên.
        throw new UnsupportedOperationException("Không hỗ trợ insert Reminder đơn lẻ. Hãy dùng insertReminders(appId, list).");
    }

    @Override
    public void update(String id, Reminder entity) {
        String sql = "UPDATE Reminder SET type = ?, minutesBefore = ?, message = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entity.getType().name());
            pstmt.setInt(2, entity.getMinutesBefore());
            pstmt.setString(3, entity.getMessage());
            pstmt.setString(4, id);

            pstmt.executeUpdate();
            System.out.println("DAO: Đã cập nhật Reminder ID: " + id);
        } catch (SQLException e) {
            System.err.println("Lỗi update Reminder SQL: " + e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM Reminder WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
            System.out.println("DAO: Đã xóa Reminder ID: " + id);
        } catch (SQLException e) {
            System.err.println("Lỗi delete Reminder SQL: " + e.getMessage());
        }
    }

    @Override
    public Reminder findById(String id) {
        String sql = "SELECT * FROM Reminder WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Reminder(
                        ReminderType.valueOf(rs.getString("type")),
                        rs.getInt("minutesBefore"),
                        rs.getString("message")
                );
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findById Reminder SQL: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Reminder> findAll() {
        List<Reminder> list = new ArrayList<>();
        String sql = "SELECT * FROM Reminder";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Reminder(
                        ReminderType.valueOf(rs.getString("type")),
                        rs.getInt("minutesBefore"),
                        rs.getString("message")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findAll Reminder SQL: " + e.getMessage());
        }
        return list;
    }
}