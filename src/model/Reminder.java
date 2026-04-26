package model;

import java.util.UUID;

public class Reminder {
    private String id;
    private ReminderType type;
    private int minutesBefore;
    private String message;

    public Reminder(ReminderType type, int minutesBefore, String message) {
        this.id = UUID.randomUUID().toString(); // Tự động sinh ID
        this.type = type;
        this.minutesBefore = minutesBefore;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public ReminderType getType() {
        return type;
    }

    public void setType(ReminderType type) {
        this.type = type;
    }

    public int getMinutesBefore() {
        return minutesBefore;
    }

    public void setMinutesBefore(int minutesBefore) {
        this.minutesBefore = minutesBefore;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}