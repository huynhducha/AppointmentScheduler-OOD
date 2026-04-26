package dao.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection connection = null;

    // Thay đổi các thông số này theo Database thực tế của bạn
    private static final String URL = "jdbc:sqlserver://localhost:3306;databaseName=AppointmentDB";
    private static final String USER = "sa";
    private static final String PASS = "huynhducha";

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Đảm bảo Driver đã được load
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                connection = DriverManager.getConnection(URL, USER, PASS);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Không tìm thấy Driver JDBC!");
            }
        }
        return connection;
    }
}