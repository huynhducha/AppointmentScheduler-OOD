-- ========================================================
-- DATABASE SCHEMA FOR APPOINTMENT MANAGEMENT SYSTEM
-- ========================================================

-- 1. Tạo Database
CREATE DATABASE IF NOT EXISTS appointmentdb
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE appointmentdb;

-- 2. Bảng User (Đã cập nhật thêm cột Password)
CREATE TABLE IF NOT EXISTS User (
                                    id VARCHAR(50) PRIMARY KEY,
    fullName VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL -- Cột mới thêm để phục vụ Login
    );

-- 3. Bảng Appointment (Lịch hẹn / Cuộc họp nhóm)
CREATE TABLE IF NOT EXISTS Appointment (
                                           id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    location VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    startTime DATETIME NOT NULL,
    endTime DATETIME NOT NULL
    );

-- 4. Bảng User_Appointment (Lịch cá nhân - Liên kết 1-n giữa User và Appointment)
CREATE TABLE IF NOT EXISTS User_Appointment (
                                                userId VARCHAR(50) NOT NULL,
    appointmentId VARCHAR(50) NOT NULL,
    PRIMARY KEY (userId, appointmentId),
    FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (appointmentId) REFERENCES Appointment(id) ON DELETE CASCADE
    );

-- 5. Bảng Group_Participant (Thành viên trong Group Meeting)
CREATE TABLE IF NOT EXISTS Group_Participant (
                                                 meetingId VARCHAR(50) NOT NULL,
    userId VARCHAR(50) NOT NULL,
    PRIMARY KEY (meetingId, userId),
    FOREIGN KEY (meetingId) REFERENCES Appointment(id) ON DELETE CASCADE,
    FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE
    );

-- 6. Bảng Reminder (Lời nhắc hẹn)
CREATE TABLE IF NOT EXISTS Reminder (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        appId VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL, -- 'POPUP' hoặc 'EMAIL'
    minutesBefore INT NOT NULL,
    message VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    FOREIGN KEY (appId) REFERENCES Appointment(id) ON DELETE CASCADE
    );

-- ========================================================
-- DỮ LIỆU MẪU (OPTIONAL - Để bạn test nhanh)
-- ========================================================
-- INSERT INTO User (id, fullName, email, password) VALUES ('U001', 'Huỳnh Đức Hà', 'ha.hd@example.com', '123456');