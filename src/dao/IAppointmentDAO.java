package dao;

import model.Appointment;
import model.GroupMeeting;
import model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface IAppointmentDAO extends IBaseDAO<Appointment, String> {
    // Chỉ chứa các hàm ĐẶC THÙ của Appointment
// Sửa hàm cũ thành:
    Appointment findConflict(LocalDateTime start, LocalDateTime end, String userId);
    // Sửa hàm cũ thành thế này (Dùng chính xác StartTime và EndTime)
    GroupMeeting findMatchingGroupMeeting(String title, LocalDateTime startTime, LocalDateTime endTime);

    // Thêm hàm mới: Lấy danh sách người tham gia của một Group Meeting
    List<User> getParticipantsByMeetingId(String meetingId);

    List<Appointment> getAppointmentsByUserId(String userId);

    // Quản lý liên kết
    void addParticipantToGroup(String meetingId, String userId);
    void addMeetingToUserCalendar(String userId, String meetingId);
}