package Java_HTTP_Demo;

import java.sql.*;

public class DBConnection {
    public static Connection getConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/user_db";
            String user = "root"; // Thay bằng username của bạn
            String pass = "112358";     // Thay bằng password của bạn
            return DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
