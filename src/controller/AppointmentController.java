package controller;

import bll.AppointmentBLL;
import model.Appointment;
import model.GroupMeeting;
import model.Reminder;

import java.util.List;

public class AppointmentController {
    private final AppointmentBLL appointmentBLL;

    // Dependency Injection: Nhận BLL từ bên ngoài
    public AppointmentController(AppointmentBLL appointmentBLL) {
        this.appointmentBLL = appointmentBLL;
    }

    // --- API 1: TẠO LỊCH MỚI ---
    public void createAppointment(Appointment newApp, List<Reminder> reminders, String userId) {
        try {
            // Bước 1: Gọi BLL kiểm tra trùng lịch
            Appointment conflict = appointmentBLL.checkConflict(newApp);

            if (conflict != null) {
                // UI sẽ nhận thông báo này và hiển thị Popup "Trùng lịch! Bạn muốn Thay thế hay Tham gia nhóm?"
                System.out.println("❌ CẢNH BÁO: Trùng lịch với cuộc hẹn ID: " + conflict.getId());
                return;
            }

            // Bước 2: Nếu không trùng, tiến hành lưu an toàn
            Appointment savedApp = appointmentBLL.processNewAppointment(newApp, reminders, userId);
            System.out.println("✅ Tạo lịch cá nhân thành công! ID Lịch: " + savedApp.getId());

        } catch (Exception e) {
            System.out.println("❌ Lỗi dữ liệu: " + e.getMessage());
        }
    }

    // --- API 2: XÁC NHẬN "THAY THẾ LỊCH CŨ" ---
    public void confirmReplaceAppointment(String oldId, Appointment newApp, List<Reminder> reminders) {
        try {
            appointmentBLL.processAppointmentReplacement(oldId, newApp, reminders);
            System.out.println("✅ Đã ghi đè lịch cũ thành công!");
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi thay thế: " + e.getMessage());
        }
    }

    // --- API 3: XÁC NHẬN "THAM GIA NHÓM" ---
    public void confirmJoinGroup(String meetingId, String userId, boolean isReplace, String oldAppId) {
        try {
            GroupMeeting meeting = appointmentBLL.processGroupJoin(meetingId, userId, isReplace, oldAppId);
            if (meeting != null) {
                System.out.println("✅ Đã tham gia nhóm họp thành công!");
            } else {
                System.out.println("❌ Lỗi: Không tìm thấy nhóm họp.");
            }
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi tham gia nhóm: " + e.getMessage());
        }
    }
}