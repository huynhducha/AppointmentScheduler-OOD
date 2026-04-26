package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Appointment {
    private String id;
    private String title;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Reminder> reminders;

    public Appointment(String id, String title, String location, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reminders = new ArrayList<>(); // Khởi tạo danh sách rỗng để tránh NullPointerException
    }

    // --- CÁC HÀM XỬ LÝ LOGIC NGHIỆP VỤ (BUSINESS LOGIC) ---

    // Kiểm tra tính hợp lệ của dữ liệu (Defense in Depth)
    public boolean isValid() {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        if (startTime == null || endTime == null) {
            return false;
        }
        // Hợp lệ nếu thời gian bắt đầu xảy ra trước thời gian kết thúc
        return startTime.isBefore(endTime);
    }

    // Tính thời lượng cuộc hẹn bằng phút
    public long getDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    // Thêm lời nhắc vào cuộc hẹn
    public void addReminder(Reminder reminder) {
        if (reminder != null) {
            this.reminders.add(reminder);
        }
    }

    // --- GETTERS & SETTERS ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public List<Reminder> getReminders() { return reminders; }
    public void setReminders(List<Reminder> reminders) { this.reminders = reminders; }
}