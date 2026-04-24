package Java_HTTP_Demo;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;

public class JavaHttpServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(9000), 0);
        server.createContext("/api/users", new UserHandler());
        server.setExecutor(null);
        System.out.println("HTTP Server đang chạy tại: http://localhost:9000/api/users");
        server.start();
    }

    static class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response = "";

            if (method.equalsIgnoreCase("GET")) {
            	String query = exchange.getRequestURI().getQuery();
            	String idStr = getValue(query, "id");
            	String user = getValue(query, "username");
            	String mail = getValue(query, "email");

            	Integer id = (idStr != null && !idStr.isEmpty()) ? Integer.parseInt(idStr) : null;

            	response = handleGetRequest(id, user, mail);
            } else if (method.equalsIgnoreCase("POST")) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes());
                
                // Đọc các giá trị từ Body
                String action = getValue(body, "action"); // Thêm tham số này
                String username = getValue(body, "username");
                String email = getValue(body, "email");
                String id = getValue(body, "id"); // Cần ID để sửa hoặc xóa

                if ("add".equals(action)) {
                    response = handleInsert(username, email);
                } else if ("update".equals(action)) {
                    response = handleUpdate(id, username, email);
                } else if ("delete".equals(action)) {
                    response = handleDelete(id);
                }
            }

            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String handleGetRequest(Integer id, String username, String email) {
            StringBuilder sb = new StringBuilder();
            // 1. Khởi tạo câu lệnh gốc
            StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
            
            if (id != null) {
                sql.append(" AND id = ?");
            }
            if (username != null && !username.isEmpty()) {
                // Thay = bằng LIKE
                sql.append(" AND username LIKE ?");
            }
            if (email != null && !email.isEmpty()) {
                // Thay = bằng LIKE
                sql.append(" AND email LIKE ?");
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
                
                // 2. Đổ dữ liệu vào các dấu "?" theo đúng thứ tự
            	int paramIndex = 1;

            	if (id != null) {
            	    pstmt.setInt(paramIndex++, id);
            	}
            	if (username != null && !username.isEmpty()) {
            	    // Thêm % vào trước và sau giá trị tìm kiếm
            	    pstmt.setString(paramIndex++, "%" + username + "%");
            	}
            	if (email != null && !email.isEmpty()) {
            	    // Thêm % vào trước và sau giá trị tìm kiếm
            	    pstmt.setString(paramIndex++, "%" + email + "%");
            	}

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    // Bạn có thể hiện thêm ID ở đây để dễ kiểm tra
                    sb.append("[").append(rs.getInt("id")).append("] ")
                      .append(rs.getString("username")).append(" - ")
                      .append(rs.getString("email")).append("\n");
                }
                
                if (sb.length() == 0) return "Không tìm thấy dữ liệu phù hợp.";
                
            } catch (Exception e) {
                return "Lỗi truy vấn: " + e.getMessage();
            }
            return sb.toString();
        }

        private String handleInsert(String user, String mail) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO users (username, email) VALUES (?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, user);
                pstmt.setString(2, mail);
                pstmt.executeUpdate();
                return "Thêm thành công user: " + user;
            } catch (Exception e) { return "Lỗi thêm dữ liệu: " + e.getMessage(); }
        }
        
     // Hàm bổ sung cho Xóa
        private String handleDelete(String id) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM users WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(id));
                pstmt.executeUpdate();
                return "Xóa thành công ID: " + id;
            } catch (Exception e) { return "Lỗi xóa: " + e.getMessage(); }
        }

        // Hàm bổ sung cho Sửa
        private String handleUpdate(String id, String user, String mail) {
            if (id == null || id.isEmpty()) return "Lỗi: Thiếu ID để cập nhật";
            
            try (Connection conn = DBConnection.getConnection()) {
                StringBuilder sql = new StringBuilder("UPDATE users SET ");
                boolean hasUser = (user != null && !user.isEmpty());
                boolean hasMail = (mail != null && !mail.isEmpty());

                if (!hasUser && !hasMail) return "Không có thông tin gì để cập nhật!";

                // Xử lý dấu phẩy động
                if (hasUser) {
                    sql.append("username = ?");
                }
                if (hasMail) {
                    if (hasUser) sql.append(", "); // Chỉ thêm phẩy nếu trước đó đã có username
                    sql.append("email = ?");
                }
                
                sql.append(" WHERE id = ?");

                PreparedStatement pstmt = conn.prepareStatement(sql.toString());
                
                // Đổ dữ liệu vào dấu ? theo thứ tự xuất hiện
                int paramIndex = 1;
                if (hasUser) {
                    pstmt.setString(paramIndex++, user);
                }
                if (hasMail) {
                    pstmt.setString(paramIndex++, mail);
                }
                
                // ID luôn ở cuối cùng
                pstmt.setInt(paramIndex, Integer.parseInt(id));

                int rows = pstmt.executeUpdate();
                if (rows > 0) return "Cập nhật thành công ID: " + id;
                else return "Không tìm thấy ID: " + id;
                
            } catch (Exception e) { 
                return "Lỗi sửa: " + e.getMessage(); 
            }
        }

        // Hàm phụ để tách giá trị từ form data
        private String getValue(String body, String key) {
            for (String pair : body.split("&")) {
                String[] kv = pair.split("=");
                if (kv[0].equals(key)) return kv.length > 1 ? kv[1] : "";
            }
            return "";
        }
    }
}