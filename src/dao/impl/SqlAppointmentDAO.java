package dao.impl;

import dao.IAppointmentDAO;
import dao.helper.DBConnection;
import model.Appointment;
import model.GroupMeeting;
import model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqlAppointmentDAO implements IAppointmentDAO {

    @Override
    public String insert(Appointment entity) {
        // 1. Kiểm tra: Nếu App chưa có ID, Backend tự sinh ra một mã UUID ngẫu nhiên
        if (entity.getId() == null || entity.getId().trim().isEmpty()) {
            entity.setId("APP-" + java.util.UUID.randomUUID().toString().substring(0, 6));
        }

        // 2. Bổ sung cột 'id' vào câu lệnh SQL và tăng số lượng dấu '?' lên 5
        String sql = "INSERT INTO Appointment (id, title, location, startTime, endTime) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 3. Truyền đủ 5 tham số xuống Database
            pstmt.setString(1, entity.getId());
            pstmt.setString(2, entity.getTitle());
            pstmt.setString(3, entity.getLocation());
            pstmt.setTimestamp(4, Timestamp.valueOf(entity.getStartTime()));
            pstmt.setTimestamp(5, Timestamp.valueOf(entity.getEndTime()));

            // 4. Thực thi câu lệnh
            pstmt.executeUpdate();

            // Trả về ID (là ID bạn truyền vào "1", "2" hoặc ID do hệ thống tự sinh)
            return entity.getId();

        } catch (SQLException e) {
            System.err.println("Lỗi Insert SQL: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Appointment findConflict(LocalDateTime start, LocalDateTime end, String userId) {
        // Kiểm tra xem khoảng thời gian này có cấn với lịch cá nhân hoặc lịch nhóm NÀO CỦA USER NÀY không
        String sql = "SELECT a.* FROM Appointment a JOIN User_Appointment ua ON a.id = ua.appointmentId " +
                "WHERE ua.userId = ? AND a.startTime < ? AND a.endTime > ? " +
                "UNION " +
                "SELECT a.* FROM Appointment a JOIN Group_Participant gp ON a.id = gp.meetingId " +
                "WHERE gp.userId = ? AND a.startTime < ? AND a.endTime > ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Tham số cho vế Lịch cá nhân
            pstmt.setString(1, userId);
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            pstmt.setTimestamp(3, Timestamp.valueOf(start));

            // Tham số cho vế Lịch nhóm
            pstmt.setString(4, userId);
            pstmt.setTimestamp(5, Timestamp.valueOf(end));
            pstmt.setTimestamp(6, Timestamp.valueOf(start));

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
    public GroupMeeting findMatchingGroupMeeting(String title, LocalDateTime startTime, LocalDateTime endTime) {
        // So sánh chính xác tên và mốc thời gian bắt đầu, kết thúc
        String sql = "SELECT * FROM Appointment WHERE title = ? AND startTime = ? AND endTime = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setTimestamp(2, Timestamp.valueOf(startTime));
            pstmt.setTimestamp(3, Timestamp.valueOf(endTime));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
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
    public List<User> getParticipantsByMeetingId(String meetingId) {
        List<User> participants = new ArrayList<>();
        // JOIN 2 bảng: Group_Participant (bảng trung gian) và User (bảng chứa thông tin)
        String sql = "SELECT u.id, u.fullName, u.email FROM User u " +
                "JOIN Group_Participant gp ON u.id = gp.userId " +
                "WHERE gp.meetingId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, meetingId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Khởi tạo User từ dữ liệu lấy được
                User u = new User(
                        rs.getString("id"),
                        rs.getString("fullName"),
                        rs.getString("email")
                );
                participants.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getParticipantsByMeetingId: " + e.getMessage());
        }
        return participants;
    }

    @Override
    public List<Appointment> getAppointmentsByUserId(String userId) {
        List<Appointment> list = new ArrayList<>();
        // Gộp (UNION) lịch cá nhân VÀ lịch nhóm mà User này tham gia
        String sql = "SELECT a.* FROM Appointment a JOIN User_Appointment ua ON a.id = ua.appointmentId WHERE ua.userId = ? " +
                "UNION " +
                "SELECT a.* FROM Appointment a JOIN Group_Participant gp ON a.id = gp.meetingId WHERE gp.userId = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, userId); // Truyền userId lần 2 cho vế UNION
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