package dao.impl;

import dao.IReminderDAO;
import model.Reminder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryReminderDAO implements IReminderDAO {
    // Bảng Reminder trong RAM
    private final List<ReminderRecord> database = new ArrayList<>();

    // Một class nội bộ (Internal class) để mô phỏng một dòng trong bảng SQL (gồm AppID và Reminder)
    private static class ReminderRecord {
        String appId;
        Reminder reminder;

        ReminderRecord(String appId, Reminder reminder) {
            this.appId = appId;
            this.reminder = reminder;
        }
    }

    @Override
    public String insert(Reminder entity) {
        // Không dùng trực tiếp hàm này, vì ta cần truyền AppId theo chuẩn Sequence Diagram
        return entity.getId();
    }

    @Override
    public void update(String id, Reminder entity) { }

    @Override
    public void delete(String id) { }

    @Override
    public Reminder findById(String id) { return null; }

    @Override
    public List<Reminder> findAll() { return new ArrayList<>(); }

    // --- CÁC HÀM ĐẶC THÙ ---

    @Override
    public void insertReminders(String appId, List<Reminder> reminders) {
        for (Reminder rem : reminders) {
            database.add(new ReminderRecord(appId, rem));
        }
        System.out.println("DAO: Đã lưu " + reminders.size() + " reminders cho Lịch " + appId);
    }

    @Override
    public void deleteRemindersByAppId(String appId) {
        database.removeIf(record -> record.appId.equals(appId));
        System.out.println("DAO: Đã xóa các reminders cũ của Lịch " + appId);
    }

    @Override
    public List<Reminder> getRemindersByAppId(String appId) {
        return database.stream()
                .filter(record -> record.appId.equals(appId))
                .map(record -> record.reminder)
                .collect(Collectors.toList());
    }
}