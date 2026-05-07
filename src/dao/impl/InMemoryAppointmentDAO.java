package dao.impl;

import dao.IAppointmentDAO;
import model.Appointment;
import model.GroupMeeting;
import model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InMemoryAppointmentDAO implements IAppointmentDAO {
    // Đây chính là "Database" mô phỏng trong RAM của chúng ta
    private final List<Appointment> database = new ArrayList<>();

    // Bảng phụ mô phỏng liên kết User - Appointment (Lưu dạng: userId_appointmentId)
    private final List<String> userMeetingLinks = new ArrayList<>();

    @Override
    public String insert(Appointment entity) {
        database.add(entity);
        return entity.getId();
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
        // Dọn dẹp cả liên kết trong bảng phụ mô phỏng
        userMeetingLinks.removeIf(link -> link.endsWith("_" + id));
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

    // --- CÁC HÀM ĐẶC THÙ ĐÃ ĐƯỢC CẬP NHẬT ---

    // SỬA LỖI: Thêm String userId cho khớp Interface và kiểm tra đúng lịch của User
    @Override
    public Appointment findConflict(LocalDateTime start, LocalDateTime end, String userId) {
        for (Appointment app : database) {
            // Chỉ kiểm tra cấn lịch với những cuộc hẹn mà User này có tham gia
            boolean isUserInvolved = userMeetingLinks.contains(userId + "_" + app.getId());

            if (isUserInvolved && app.getStartTime().isBefore(end) && app.getEndTime().isAfter(start)) {
                return app;
            }
        }
        return null;
    }

    @Override
    public GroupMeeting findMatchingGroupMeeting(String title, LocalDateTime startTime, LocalDateTime endTime) {
        for (Appointment app : database) {
            if (app instanceof GroupMeeting) {
                GroupMeeting meeting = (GroupMeeting) app;
                if (meeting.getTitle() != null && meeting.getTitle().equalsIgnoreCase(title) &&
                        meeting.getStartTime().equals(startTime) &&
                        meeting.getEndTime().equals(endTime)) {
                    return meeting;
                }
            }
        }
        return null;
    }

    @Override
    public List<User> getParticipantsByMeetingId(String meetingId) {
        Appointment app = findById(meetingId);
        if (app instanceof GroupMeeting) {
            return ((GroupMeeting) app).getParticipants();
        }
        return new ArrayList<>();
    }

    // Nâng cấp luôn hàm này để nó lọc đúng lịch của User (thay vì trả về tất cả)
    @Override
    public List<Appointment> getAppointmentsByUserId(String userId) {
        List<Appointment> userApps = new ArrayList<>();
        for (Appointment app : database) {
            if (userMeetingLinks.contains(userId + "_" + app.getId())) {
                userApps.add(app);
            }
        }
        return userApps;
    }

    @Override
    public void addParticipantToGroup(String meetingId, String userId) {
        Appointment app = findById(meetingId);
        if (app instanceof GroupMeeting) {
            // Mô phỏng thêm 1 user giả vào RAM để lát nữa UI hiển thị được
            ((GroupMeeting) app).addParticipant(new User(userId, "User " + userId, "dummy@email.com"));
        }
        System.out.println("DAO (RAM): Đã thêm User " + userId + " vào GroupMeeting " + meetingId);
    }

    @Override
    public void addMeetingToUserCalendar(String userId, String meetingId) {
        String link = userId + "_" + meetingId;
        if (!userMeetingLinks.contains(link)) {
            userMeetingLinks.add(link);
        }
        System.out.println("DAO (RAM): Đã liên kết Lịch " + meetingId + " cho User " + userId);
    }
}