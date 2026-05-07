package bll;

import dao.IUserDAO;
import model.User;

public class UserBLL
{
    private final IUserDAO userDAO;

    public UserBLL(IUserDAO userDAO)
    {
        this.userDAO = userDAO;
    }

    // --- SỬA HÀM LOGIN THÊM THAM SỐ PASSWORD ---
    public User login(String email, String password)
    {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty())
        {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ Email và Mật khẩu!");
        }

        User user = userDAO.findByEmail(email);

        // Kiểm tra xem User có tồn tại không và Mật khẩu có khớp không
        if (user == null || user.getPassword() == null || !user.getPassword().equals(password))
        {
            throw new RuntimeException("Email hoặc mật khẩu không chính xác!");
        }

        return user;
    }

    // --- THÊM ĐIỀU KIỆN RÀNG BUỘC KHI ĐĂNG KÝ ---
    public boolean register(User newUser)
    {
        if (newUser == null || newUser.getEmail() == null || newUser.getFullName() == null || newUser.getPassword() == null)
        {
            throw new IllegalArgumentException("Thông tin đăng ký không được để trống!");
        }

        if (newUser.getPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự!");
        }

        if (userDAO.isEmailExists(newUser.getEmail()))
        {
            throw new RuntimeException("Email này đã được đăng ký trong hệ thống!");
        }

        return userDAO.insert(newUser) != null;
    }
}