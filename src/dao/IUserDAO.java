package dao;

import model.User;

public interface IUserDAO extends IBaseDAO<User, String>
{
    // Tìm kiếm người dùng dựa trên email
    User findByEmail(String email);

    // Kiểm tra xem email đã tồn tại trong hệ thống chưa (dùng cho Đăng ký)
    boolean isEmailExists(String email);
}