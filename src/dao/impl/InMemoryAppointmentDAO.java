package dao.impl;

import dao.IAppointmentDAO;
import model.Appointment;
import model.GroupMeeting;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InMemoryAppointmentDAO implements IAppointmentDAO {
    // Đây chính là "Database" mô phỏng trong RAM của chúng ta
    private final List<Appointment> database = new ArrayList<>();

    // Bảng phụ mô phỏng liên kết User - Appointment
    private final List<String> userMeetingLinks = new ArrayList<>();

    @Override
    public String insert(Appointment entity) {
        database.add(entity);
        return entity.getId(); // Trả về ID đúng như thiết kế Sequence Diagram
    }

    @Override
    public void update(String id, Appointment newEntity) {
        for (int i = 0; i < database.size(); i++) {
            if (database.get(i).getId().equals(id)) {
                database.set(i, newEntity);
                return;
            }
        }
    }

    @Override
    public void delete(String id) {
        database.removeIf(app -> app.getId().equals(id));
    }

    @Override
    public Appointment findById(String id) {
        return database.stream()
                .filter(app -> app.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Appointment> findAll() {
        return new ArrayList<>(database);
    }

    // --- CÁC HÀM ĐẶC THÙ ---

    @Override
    public Appointment findConflict(LocalDateTime start, LocalDateTime end) {
        for (Appointment app : database) {
            // Thuật toán kiểm tra giao nhau (Overlap) của 2 khoảng thời gian
            if (app.getStartTime().isBefore(end) && app.getEndTime().isAfter(start)) {
                return app; // Tìm thấy 1 lịch bị trùng
            }
        }
        return null;
    }

    @Override
    public GroupMeeting findMatchingGroupMeeting(String title, long duration) {
        for (Appointment app : database) {
            if (app instanceof GroupMeeting) {
                GroupMeeting meeting = (GroupMeeting) app;
                if (meeting.isMatching(title, duration)) {
                    return meeting;
                }
            }
        }
        return null;
    }

    @Override
    public List<Appointment> getAppointmentsByUserId(String userId) {
        // Trong thực tế sẽ dùng lệnh SELECT JOIN, ở đây ta trả về toàn bộ cho đơn giản
        return new ArrayList<>(database);
    }

    @Override
    public void addParticipantToGroup(String meetingId, String userId) {
        Appointment app = findById(meetingId);
        // Trong hệ thống thực, ta sẽ gọi entity.addParticipant() hoặc thêm vào bảng phụ
        System.out.println("DAO: Đã thêm User " + userId + " vào GroupMeeting " + meetingId);
    }

    @Override
    public void addMeetingToUserCalendar(String userId, String meetingId) {
        userMeetingLinks.add(userId + "_" + meetingId);
        System.out.println("DAO: Đã liên kết Lịch " + meetingId + " cho User " + userId);
    }
}