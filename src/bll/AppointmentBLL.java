package bll;

import dao.IAppointmentDAO;
import dao.IReminderDAO;
import model.Appointment;
import model.GroupMeeting;
import model.Reminder;
import model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentBLL
{

    // Khai báo các Interface DAO, TUYỆT ĐỐI không khai báo class Implementation ở đây
    private final IAppointmentDAO appointmentDAO;
    private final IReminderDAO reminderDAO;

    // Kỹ thuật Dependency Injection (Tiêm phụ thuộc) thông qua Constructor
    public AppointmentBLL(IAppointmentDAO appointmentDAO, IReminderDAO reminderDAO)
    {
        this.appointmentDAO = appointmentDAO;
        this.reminderDAO = reminderDAO;
    }

    // =======================================================
    // NHÓM TRUY VẤN (READ)
    // =======================================================

    // Lấy toàn bộ danh sách lịch hẹn để hiển thị lên UI
    public List<Appointment> getAllAppointments()
    {
        return appointmentDAO.findAll();
    }

    // --- BƯỚC 1: KIỂM TRA TRÙNG LỊCH CÁ NHÂN ---
    // Thêm String userId vào tham số
    public Appointment checkConflict(Appointment newApp, String userId)
    {
        if (!newApp.isValid())
        {
            throw new IllegalArgumentException("Dữ liệu cuộc hẹn không hợp lệ! (Vui lòng kiểm tra tên và giờ)");
        }
        // Truyền userId xuống DAO
        return appointmentDAO.findConflict(newApp.getStartTime(), newApp.getEndTime(), userId);
    }

    // --- BƯỚC 2: TÌM GROUP MEETING TRÙNG KHỚP ---
    public GroupMeeting findMatchingGroupMeeting(String title, LocalDateTime startTime, LocalDateTime endTime) {
        if (title == null || title.trim().isEmpty() || startTime == null || endTime == null) {
            return null;
        }
        // Gọi DAO tìm Group
        GroupMeeting group = appointmentDAO.findMatchingGroupMeeting(title, startTime, endTime);

        // Nếu tìm thấy, lập tức tải luôn danh sách thành viên lên (để lát nữa UI hiển thị)
        if (group != null) {
            List<User> participants = appointmentDAO.getParticipantsByMeetingId(group.getId());
            group.setParticipants(participants);
        }
        return group;
    }

    // =======================================================
    // NHÓM XỬ LÝ LƯU TRỮ VÀ NGHIỆP VỤ PHỨC TẠP (WRITE)
    // =======================================================

    // 1. Luồng Thêm mới hoàn toàn
    public Appointment processNewAppointment(Appointment newApp, List<Reminder> reminders, String userId)
    {
        // Insert cuộc hẹn xuống DB và lấy ID tự động sinh ra
        String generatedId = appointmentDAO.insert(newApp);

        if (generatedId == null)
        {
            throw new RuntimeException("Không thể lưu lịch hẹn vào CSDL!");
        }

        newApp.setId(generatedId); // Cập nhật lại ID cho đối tượng trên RAM

        // Nếu đây là họp Nhóm, lập tức đưa người tạo vào danh sách tham gia
        if (newApp instanceof GroupMeeting) {
            appointmentDAO.addParticipantToGroup(generatedId, userId);
        }

        // Gắn cuộc hẹn này vào danh sách lịch cá nhân của User
        appointmentDAO.addMeetingToUserCalendar(userId, generatedId);

        // Chỉ thêm Reminder nếu người dùng có cài đặt lời nhắc
        if (reminders != null && !reminders.isEmpty())
        {
            // Giả định hàm này sẽ được triển khai trong SqlReminderDAO
            // reminderDAO.insertReminders(generatedId, reminders);
        }

        return newApp;
    }

    // 2. Luồng Thay thế lịch cũ
    public Appointment processAppointmentReplacement(String oldId, Appointment newApp, List<Reminder> reminders)
    {
        // Bắt buộc giữ lại ID cũ để ghi đè dữ liệu, tránh tạo rác trong DB
        newApp.setId(oldId);

        // Bước 1: Dọn dẹp sạch sẽ các lời nhắc của cuộc hẹn cũ
        reminderDAO.deleteRemindersByAppId(oldId);

        // Bước 2: Cập nhật thông tin cuộc hẹn bằng dữ liệu mới
        appointmentDAO.update(oldId, newApp);

        // Bước 3: Lưu danh sách lời nhắc mới
        if (reminders != null && !reminders.isEmpty())
        {
            reminderDAO.insertReminders(oldId, reminders);
        }

        return newApp;
    }

    // 3. Luồng Tham gia nhóm (Vừa Join Group, vừa Replace lịch cá nhân bị trùng nếu có)
    public GroupMeeting processGroupJoin(String meetingId, String userId, boolean isReplace, String oldAppId)
    {

        // Nếu trước đó người dùng gặp trùng lịch và chọn "Thay thế"
        if (isReplace && oldAppId != null)
        {
            // reminderDAO.deleteRemindersByAppId(oldAppId); // Dọn dẹp lời nhắc của lịch cá nhân đó
            appointmentDAO.delete(oldAppId); // Xóa lịch cá nhân
        }

        // Đăng ký User vào danh sách họp nhóm
        appointmentDAO.addParticipantToGroup(meetingId, userId);

        // Phải liên kết cuộc họp nhóm này vào lịch của User để họ còn nhìn thấy
        appointmentDAO.addMeetingToUserCalendar(userId, meetingId);

        // Truy vấn lại thông tin nhóm họp để trả về cho giao diện hiển thị
        Appointment meeting = appointmentDAO.findById(meetingId);
        if (meeting instanceof GroupMeeting)
        {
            return (GroupMeeting) meeting;
        }
        return null;
    }

    // =======================================================
    // NHÓM XÓA (DELETE)
    // =======================================================

    // Xóa một lịch hẹn an toàn (Bao gồm cả việc dọn dẹp Reminder)
    public void deleteAppointment(String id)
    {
        if (id == null || id.trim().isEmpty())
        {
            throw new IllegalArgumentException("ID cuộc hẹn không hợp lệ để xóa!");
        }

        // Luôn phải dọn dẹp các lời nhắc (bảng phụ) trước khi xóa lịch (bảng chính) để tránh lỗi khóa ngoại (Foreign Key)
        // reminderDAO.deleteRemindersByAppId(id);

        // Xóa lịch hẹn
        appointmentDAO.delete(id);
    }

    public void updateAppointment(String id, Appointment updatedApp, List<Reminder> newReminders)
    {
        if (id == null || !updatedApp.isValid())
        {
            throw new IllegalArgumentException("Dữ liệu cập nhật không hợp lệ!");
        }

        // 1. Cập nhật thông tin chính
        appointmentDAO.update(id, updatedApp);

        // 2. Cập nhật Reminder (Xóa cũ, thêm mới để đồng bộ)
        reminderDAO.deleteRemindersByAppId(id);
        if (newReminders != null && !newReminders.isEmpty())
        {
            reminderDAO.insertReminders(id, newReminders);
        }
    }

    // Lấy danh sách thành viên của một sự kiện (Dùng cho tính năng Double-Click)
    public List<User> getParticipants(String meetingId) {
        if (meetingId == null || meetingId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return appointmentDAO.getParticipantsByMeetingId(meetingId);
    }

    // Lấy toàn bộ lịch (cá nhân + nhóm) của một User cụ thể
    public List<Appointment> getUserAppointments(String userId) {
        if (userId == null || userId.isEmpty()) return new ArrayList<>();
        return appointmentDAO.getAppointmentsByUserId(userId);
    }
}