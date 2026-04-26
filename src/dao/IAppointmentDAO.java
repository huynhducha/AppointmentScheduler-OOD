package dao;

import model.Appointment;
import model.GroupMeeting;
import java.time.LocalDateTime;
import java.util.List;

public interface IAppointmentDAO extends IBaseDAO<Appointment, String> {
    // Chỉ chứa các hàm ĐẶC THÙ của Appointment
    Appointment findConflict(LocalDateTime start, LocalDateTime end);
    GroupMeeting findMatchingGroupMeeting(String title, long duration);
    List<Appointment> getAppointmentsByUserId(String userId);

    // Quản lý liên kết
    void addParticipantToGroup(String meetingId, String userId);
    void addMeetingToUserCalendar(String userId, String meetingId);
}