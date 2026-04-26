package dao.impl;

import dao.IAppointmentDAO;
import dao.helper.DBConnection;
import model.Appointment;
import model.GroupMeeting;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqlAppointmentDAO implements IAppointmentDAO {

    @Override
    public String insert(Appointment entity) {
        String sql = "INSERT INTO Appointment (title, location, startTime, endTime) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, entity.getTitle());
            pstmt.setString(2, entity.getLocation());
            pstmt.setTimestamp(3, Timestamp.valueOf(entity.getStartTime()));
            pstmt.setTimestamp(4, Timestamp.valueOf(entity.getEndTime()));

            pstmt.executeUpdate();

            // Lấy ID tự động sinh từ Database (Auto-increment)
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    String generatedId = rs.getString(1);
                    entity.setId(generatedId);
                    return generatedId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Appointment findConflict(LocalDateTime start, LocalDateTime end) {
        // Thuật toán SQL: Kiểm tra trùng lặp khoảng thời gian
        String sql = "SELECT * FROM Appointment WHERE startTime < ? AND endTime > ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(end));
            pstmt.setTimestamp(2, Timestamp.valueOf(start));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Map dữ liệu từ ResultSet sang Object Appointment (đã viết ở model)
                return new Appointment(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("location"),
                        rs.getTimestamp("startTime").toLocalDateTime(),
                        rs.getTimestamp("endTime").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void update(String id, Appointment entity) {
        String sql = "UPDATE Appointment SET title = ?, location = ?, startTime = ?, endTime = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entity.getTitle());
            pstmt.setString(2, entity.getLocation());
            pstmt.setTimestamp(3, Timestamp.valueOf(entity.getStartTime()));
            pstmt.setTimestamp(4, Timestamp.valueOf(entity.getEndTime()));
            pstmt.setString(5, id);

            pstmt.executeUpdate();
            System.out.println("DAO: Đã cập nhật thành công lịch trình DB ID: " + id);
        } catch (SQLException e) {
            System.err.println("Lỗi update SQL: " + e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM Appointment WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
            System.out.println("DAO: Đã xóa lịch trình DB ID: " + id);
        } catch (SQLException e) {
            System.err.println("Lỗi delete SQL: " + e.getMessage());
        }
    }

    @Override
    public Appointment findById(String id) {
        String sql = "SELECT * FROM Appointment WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Appointment(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("location"),
                        rs.getTimestamp("startTime").toLocalDateTime(),
                        rs.getTimestamp("endTime").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findById SQL: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM Appointment";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Appointment(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("location"),
                        rs.getTimestamp("startTime").toLocalDateTime(),
                        rs.getTimestamp("endTime").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findAll SQL: " + e.getMessage());
        }
        return list;
    }

    @Override
    public GroupMeeting findMatchingGroupMeeting(String title, long duration) {
        // Dùng hàm DATEDIFF của SQL Server để tính số phút giữa startTime và endTime
        String sql = "SELECT * FROM Appointment WHERE title = ? AND DATEDIFF(MINUTE, startTime, endTime) = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setLong(2, duration);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Ép kiểu dữ liệu đọc được sang GroupMeeting
                return new GroupMeeting(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("location"),
                        rs.getTimestamp("startTime").toLocalDateTime(),
                        rs.getTimestamp("endTime").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            System.err.println("Lỗi findMatchingGroupMeeting SQL: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Appointment> getAppointmentsByUserId(String userId) {
        List<Appointment> list = new ArrayList<>();
        // Dùng lệnh JOIN để lấy lịch của một User cụ thể
        String sql = "SELECT a.* FROM Appointment a " +
                "JOIN User_Appointment ua ON a.id = ua.appointmentId " +
                "WHERE ua.userId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new Appointment(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("location"),
                        rs.getTimestamp("startTime").toLocalDateTime(),
                        rs.getTimestamp("endTime").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAppointmentsByUserId SQL: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void addParticipantToGroup(String meetingId, String userId) {
        String sql = "INSERT INTO Group_Participant (meetingId, userId) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, meetingId);
            pstmt.setString(2, userId);
            pstmt.executeUpdate();
            System.out.println("DAO: Đã thêm User " + userId + " vào GroupMeeting " + meetingId + " trong DB.");
        } catch (SQLException e) {
            System.err.println("Lỗi addParticipantToGroup SQL: " + e.getMessage());
        }
    }

    @Override
    public void addMeetingToUserCalendar(String userId, String meetingId) {
        String sql = "INSERT INTO User_Appointment (userId, appointmentId) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, meetingId);
            pstmt.executeUpdate();
            System.out.println("DAO: Đã liên kết Lịch " + meetingId + " cho User " + userId + " trong DB.");
        } catch (SQLException e) {
            System.err.println("Lỗi addMeetingToUserCalendar SQL: " + e.getMessage());
        }
    }
}