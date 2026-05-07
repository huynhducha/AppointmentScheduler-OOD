package model;

public class User {
    private String id;
    private String fullName;
    private String email;
    private String password; // --- BIẾN MỚI ---

    // 1. Constructor 3 tham số (Dùng cho List/View để không rò rỉ password)
    public User(String id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    // 2. Constructor 4 tham số (Dùng riêng cho Đăng nhập / Đăng ký)
    public User(String id, String fullName, String email, String password) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    // --- GETTERS & SETTERS (Giữ nguyên cái cũ, thêm cái mới) ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}