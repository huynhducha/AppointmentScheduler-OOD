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

    // Hàm tự kiểm tra xem cuộc họp này có khớp với tên và thời lượng tìm kiếm không
    public boolean isMatching(String searchTitle, long searchDuration) {
        boolean isTitleMatch = this.getTitle() != null && this.getTitle().equalsIgnoreCase(searchTitle);
        boolean isDurationMatch = this.getDurationInMinutes() == searchDuration;

        return isTitleMatch && isDurationMatch;
    }

    // Thêm người dùng vào cuộc họp
    public void addParticipant(User user) {
        if (user != null && !participants.contains(user)) {
            this.participants.add(user);
        }
    }

    // Cho phép xóa người tham gia khỏi nhóm
    public void removeParticipant(User user) {
        if (user != null) {
            this.participants.remove(user);
        }
    }

    // Lấy danh sách người tham gia để hiển thị lên UI (Defensive Copy)
    public List<User> getParticipants() {
        return new ArrayList<>(participants);
    }

    // Chỉ giữ lại Setter
    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }
}