package dao;

import model.Reminder;
import java.util.List;

public interface IReminderDAO extends IBaseDAO<Reminder, String> {
    // Chỉ chứa các hàm ĐẶC THÙ của Reminder
    void insertReminders(String appId, List<Reminder> reminders);
    void deleteRemindersByAppId(String appId);
    List<Reminder> getRemindersByAppId(String appId);
}