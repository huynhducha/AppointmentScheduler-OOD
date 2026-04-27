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

    // Nghiệp vụ 1: Định danh người dùng qua Email
    public User loginByEmail(String email)
    {
        if (email == null || email.trim().isEmpty())
        {
            throw new IllegalArgumentException("Vui lòng nhập địa chỉ email!");
        }

        User user = userDAO.findByEmail(email);

        if (user == null)
        {
            throw new RuntimeException("Không tìm thấy tài khoản nào liên kết với email này!");
        }

        return user;
    }

    // Nghiệp vụ 2: Đăng ký người dùng mới
    public boolean register(User newUser)
    {
        if (newUser == null || newUser.getEmail() == null || newUser.getFullName() == null)
        {
            throw new IllegalArgumentException("Thông tin người dùng không được để trống!");
        }

        if (userDAO.isEmailExists(newUser.getEmail()))
        {
            throw new RuntimeException("Email này đã được đăng ký trong hệ thống!");
        }

        return userDAO.insert(newUser) != null;
    }
}