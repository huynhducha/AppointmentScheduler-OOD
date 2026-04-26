package bll;

import dao.IAppointmentDAO;
import dao.IReminderDAO;
import model.Appointment;
import model.GroupMeeting;
import model.Reminder;

import java.util.List;

public class AppointmentBLL {
    // Khai báo các Interface DAO, TUYỆT ĐỐI không khai báo class Implementation ở đây
    private final IAppointmentDAO appointmentDAO;
    private final IReminderDAO reminderDAO;

    // Kỹ thuật Dependency Injection (Tiêm phụ thuộc) thông qua Constructor
    public AppointmentBLL(IAppointmentDAO appointmentDAO, IReminderDAO reminderDAO) {
        this.appointmentDAO = appointmentDAO;
        this.reminderDAO = reminderDAO;
    }

    // --- BƯỚC 1: KIỂM TRA TRÙNG LỊCH ---
    public Appointment checkConflict(Appointment newApp) {
        if (!newApp.isValid()) {
            throw new IllegalArgumentException("Dữ liệu cuộc hẹn không hợp lệ!");
        }
        return appointmentDAO.findConflict(newApp.getStartTime(), newApp.getEndTime());
    }

    // --- BƯỚC 2: TÌM GROUP MEETING ---
    public GroupMeeting findMatchingGroupMeeting(String title, long duration) {
        if (title == null || title.trim().isEmpty() || duration <= 0) {
            return null;
        }
        return appointmentDAO.findMatchingGroupMeeting(title, duration);
    }

    // --- BƯỚC 3: CÁC HÀM XỬ LÝ NGHIỆP VỤ LƯU TRỮ PHỨC TẠP ---

    // 1. Luồng Thêm mới hoàn toàn (Normal Case - Bước 32 đến 37)
    public Appointment processNewAppointment(Appointment newApp, List<Reminder> reminders, String userId) {
        // Insert cuộc hẹn xuống DB và lấy ID tự động sinh ra
        String generatedId = appointmentDAO.insert(newApp);
        newApp.setId(generatedId); // Cập nhật lại ID cho đối tượng trên RAM

        // Gắn cuộc hẹn này vào danh sách lịch cá nhân của User
        appointmentDAO.addMeetingToUserCalendar(userId, generatedId);

        // Chỉ thêm Reminder nếu người dùng có cài đặt lời nhắc
        if (reminders != null && !reminders.isEmpty()) {
            reminderDAO.insertReminders(generatedId, reminders);
        }

        return newApp;
    }

    // 2. Luồng Thay thế lịch cũ (Replace Case - Bước 27 đến 31)
    public Appointment processAppointmentReplacement(String oldId, Appointment newApp, List<Reminder> reminders) {
        // Bắt buộc giữ lại ID cũ để ghi đè dữ liệu, tránh tạo rác trong DB
        newApp.setId(oldId);

        // Bước 1: Dọn dẹp sạch sẽ các lời nhắc của cuộc hẹn cũ
        reminderDAO.deleteRemindersByAppId(oldId);

        // Bước 2: Cập nhật thông tin cuộc hẹn bằng dữ liệu mới
        appointmentDAO.update(oldId, newApp);

        // Bước 3: Lưu danh sách lời nhắc mới
        if (reminders != null && !reminders.isEmpty()) {
            reminderDAO.insertReminders(oldId, reminders);
        }

        return newApp;
    }

    // 3. Luồng Tham gia nhóm (Join Group - Bước 21 đến 26)
    // Tích hợp xử lý góc khuất (Edge case): Vừa Join Group, vừa Replace lịch cá nhân bị trùng
    public GroupMeeting processGroupJoin(String meetingId, String userId, boolean isReplace, String oldAppId) {

        // Nếu trước đó người dùng gặp trùng lịch và chọn "Thay thế"
        if (isReplace && oldAppId != null) {
            appointmentDAO.delete(oldAppId); // Xóa lịch cá nhân
            reminderDAO.deleteRemindersByAppId(oldAppId); // Dọn dẹp lời nhắc của lịch đó
        }

        // Đăng ký User vào danh sách họp nhóm
        appointmentDAO.addParticipantToGroup(meetingId, userId);

        // Quan trọng: Phải liên kết cuộc họp nhóm này vào lịch của User để họ còn nhìn thấy
        appointmentDAO.addMeetingToUserCalendar(userId, meetingId);

        // Truy vấn lại thông tin nhóm họp để trả về cho giao diện (UI) hiển thị
        Appointment meeting = appointmentDAO.findById(meetingId);
        if (meeting instanceof GroupMeeting) {
            return (GroupMeeting) meeting;
        }
        return null;
    }
}