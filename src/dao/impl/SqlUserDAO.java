package dao.impl;

import dao.IUserDAO;
import dao.helper.DBConnection;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlUserDAO implements IUserDAO
{

    // --- CÁC HÀM TỪ IUserDAO ---

    @Override
    public User findByEmail(String email)
    {
        String sql = "SELECT * FROM User WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
            {
                return new User(
                        rs.getString("id"),
                        rs.getString("fullName"),
                        rs.getString("email")
                );
            }
        } catch (SQLException e)
        {
            System.err.println("Lỗi findByEmail SQL: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean isEmailExists(String email)
    {
        String sql = "SELECT id FROM User WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e)
        {
            System.err.println("Lỗi isEmailExists SQL: " + e.getMessage());
        }
        return false;
    }

    // --- CÁC HÀM OVERRIDE TỪ IBaseDAO<User, String> ---

    @Override
    public String insert(User entity)
    {
        // Tự động sinh ID nếu chưa có
        if (entity.getId() == null || entity.getId().trim().isEmpty())
        {
            entity.setId("USR-" + java.util.UUID.randomUUID().toString().substring(0, 6));
        }

        String sql = "INSERT INTO User (id, fullName, email) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {

            pstmt.setString(1, entity.getId());
            pstmt.setString(2, entity.getFullName());
            pstmt.setString(3, entity.getEmail());

            pstmt.executeUpdate();
            return entity.getId();
        } catch (SQLException e)
        {
            System.err.println("Lỗi insert User SQL: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void update(String id, User entity)
    {
        String sql = "UPDATE User SET fullName = ?, email = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {

            pstmt.setString(1, entity.getFullName());
            pstmt.setString(2, entity.getEmail());
            pstmt.setString(3, id);

            pstmt.executeUpdate();
            System.out.println("DAO: Đã cập nhật User ID: " + id);
        } catch (SQLException e)
        {
            System.err.println("Lỗi update User SQL: " + e.getMessage());
        }
    }

    @Override
    public void delete(String id)
    {
        String sql = "DELETE FROM User WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {

            pstmt.setString(1, id);
            pstmt.executeUpdate();
            System.out.println("DAO: Đã xóa User ID: " + id);
        } catch (SQLException e)
        {
            System.err.println("Lỗi delete User SQL: " + e.getMessage());
        }
    }

    @Override
    public User findById(String id)
    {
        String sql = "SELECT * FROM User WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
            {
                return new User(
                        rs.getString("id"),
                        rs.getString("fullName"),
                        rs.getString("email")
                );
            }
        } catch (SQLException e)
        {
            System.err.println("Lỗi findById User SQL: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<User> findAll()
    {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM User";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql))
        {

            while (rs.next())
            {
                list.add(new User(
                        rs.getString("id"),
                        rs.getString("fullName"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e)
        {
            System.err.println("Lỗi findAll User SQL: " + e.getMessage());
        }
        return list;
    }
}