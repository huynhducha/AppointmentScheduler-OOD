package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GroupMeeting extends Appointment {
    private List<User> participants;

    public GroupMeeting(String id, String title, String location, LocalDateTime startTime, LocalDateTime endTime) {
        // Gọi Constructor của lớp cha (Appointment)
        super(id, title, location, startTime, endTime);
        this.participants = new ArrayList<>();
    }

    // --- CÁC HÀM XỬ LÝ LOGIC NGHIỆP VỤ ---

    // Thêm người dùng vào cuộc họp
    public void addParticipant(User user) {
        if (user != null && !participants.contains(user)) {
            this.participants.add(user);
        }
    }

    // Hàm tự kiểm tra xem cuộc họp này có khớp với tên và thời lượng tìm kiếm không
    public boolean isMatching(String searchTitle, long searchDuration) {
        boolean isTitleMatch = this.getTitle() != null && this.getTitle().equalsIgnoreCase(searchTitle);
        boolean isDurationMatch = this.getDurationInMinutes() == searchDuration;

        return isTitleMatch && isDurationMatch;
    }

    // --- GETTERS & SETTERS ---

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }
}