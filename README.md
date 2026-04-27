# 📅 Appointment Scheduler (OOD Project)

Hệ thống quản lý lịch hẹn cá nhân và họp nhóm, được xây dựng dựa trên kiến trúc 3-Tier (MVC + DAO + BLL) bằng JavaFX và MySQL.

## 🚀 Tính năng nổi bật
- **Quản lý người dùng**: Đăng ký, đăng nhập và quản lý phiên làm việc (Session).
- **Lịch trình thông minh**: Trực quan hóa lịch hẹn trên Calendar Grid.
- **Xử lý xung đột (Conflict Handling)**: Tự động phát hiện trùng lịch cá nhân và gợi ý tham gia nhóm (Group Meeting).
- **Hệ thống nhắc hẹn (Background Reminder)**: Luồng chạy ngầm tự động thông báo trước khi sự kiện diễn ra.

## 🛠 Công nghệ sử dụng
- **Ngôn ngữ**: Java 21
- **Giao diện**: JavaFX (FXML + Controller)
- **Cơ sở dữ liệu**: MySQL 8.x (JDBC thuần)
- **Thiết kế**: Object-Oriented Design (OOD), Dependency Injection cơ bản.

## ⚙️ Hướng dẫn cài đặt
1. Clone repository này về máy.
2. Mở MySQL Workbench, chạy toàn bộ script trong file `database/schema.sql` để tạo CSDL.
3. Cấu hình lại thông tin `username` và `password` MySQL trong file `src/dao/helper/DBConnection.java`.
4. Mở project bằng IntelliJ IDEA, cấu hình thư viện JavaFX và chạy file `Main.java`.
