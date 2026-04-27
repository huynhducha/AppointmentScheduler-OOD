package utils;

import model.User;

public class SessionManager
{
    // Biến static để giữ thông tin user trên RAM suốt quá trình app chạy
    private static User currentUser;

    public static void setCurrentUser(User user)
    {
        currentUser = user;
    }

    public static User getCurrentUser()
    {
        return currentUser;
    }

    // Hàm dùng khi đăng xuất
    public static void clearSession()
    {
        currentUser = null;
    }
}