-- Tạo database nếu chưa có và sử dụng nó
CREATE DATABASE IF NOT EXISTS appointmentdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE appointmentdb;

-- 1. Bảng User (Người dùng)
CREATE TABLE IF NOT EXISTS User (
    id VARCHAR(50) PRIMARY KEY,
    fullName VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

-- 2. Bảng Appointment (Lịch hẹn / Cuộc họp nhóm)
CREATE TABLE IF NOT EXISTS Appointment (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    location VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    startTime DATETIME NOT NULL,
    endTime DATETIME NOT NULL
);

-- 3. Bảng User_Appointment (Lịch cá nhân của User)
CREATE TABLE IF NOT EXISTS User_Appointment (
    userId VARCHAR(50) NOT NULL,
    appointmentId VARCHAR(50) NOT NULL,
    PRIMARY KEY (userId, appointmentId),
    FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (appointmentId) REFERENCES Appointment(id) ON DELETE CASCADE
);

-- 4. Bảng Group_Participant (Thành viên tham gia Group Meeting)
CREATE TABLE IF NOT EXISTS Group_Participant (
    meetingId VARCHAR(50) NOT NULL,
    userId VARCHAR(50) NOT NULL,
    PRIMARY KEY (meetingId, userId),
    FOREIGN KEY (meetingId) REFERENCES Appointment(id) ON DELETE CASCADE,
    FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE
);

-- 5. Bảng Reminder (Lời nhắc hẹn)
CREATE TABLE IF NOT EXISTS Reminder (
    id INT AUTO_INCREMENT PRIMARY KEY,
    appId VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL, -- 'POPUP' hoặc 'EMAIL'
    minutesBefore INT NOT NULL,
    message VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    FOREIGN KEY (appId) REFERENCES Appointment(id) ON DELETE CASCADE
);