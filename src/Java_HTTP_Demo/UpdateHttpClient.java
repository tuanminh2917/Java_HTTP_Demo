package Java_HTTP_Demo;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.*;

public class UpdateHttpClient {
    private JFrame frame;
    private JTextField idTextField, usernameTextField, emailTextField;
    private JCheckBox chkLockUser, chkLockEmail;
    private Runnable onUpdateSuccess; // Dùng để thông báo cho cửa sổ chính load lại data

    public UpdateHttpClient(Runnable onUpdateSuccess) {
        this.onUpdateSuccess = onUpdateSuccess;
        frame = new JFrame("Thêm Người Dùng Mới");
        frame.setSize(400, 200);
        frame.setLayout(new GridLayout(4, 3, 10, 10));

        usernameTextField = new JTextField();
        emailTextField = new JTextField();
        idTextField = new JTextField();
        JButton btn = new JButton("Sửa");
        
        // Tạo Checkbox "Giữ nguyên"
        chkLockUser = new JCheckBox("Khóa");
        chkLockEmail = new JCheckBox("Khóa");

        frame.add(new JLabel(" ID cần sửa:")); frame.add(idTextField); frame.add(new JLabel("")); 
        frame.add(new JLabel(" Username mới:")); frame.add(usernameTextField); frame.add(chkLockUser);
        frame.add(new JLabel(" Email mới:")); frame.add(emailTextField); frame.add(chkLockEmail);
        frame.add(btn);
        
        chkLockUser.addActionListener(e -> {
            // Nếu chọn "Khóa" (isSelected), disable TextField
            usernameTextField.setEnabled(!chkLockUser.isSelected());
        });

        // Sự kiện cho Checkbox Email
        chkLockEmail.addActionListener(e -> {
            emailTextField.setEnabled(!chkLockEmail.isSelected());
        });

        btn.addActionListener(e -> performUpdate());
    }

    private void performUpdate() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            // BỔ SUNG: thêm action=add vào đây
            String postData = "action=update" +
            				  "&id=" + idTextField.getText() +
                              "&username=" + usernameTextField.getText() + 
                              "&email=" + emailTextField.getText();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:9000/api/users")) // Đảm bảo cổng 9000 khớp với Server
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(postData))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Bây giờ response.body() sẽ có giá trị "Thêm thành công user: ..."
            JOptionPane.showMessageDialog(frame, response.body());
            
            if (response.body().contains("thành công")) {
                frame.dispose(); 
                onUpdateSuccess.run(); 
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Lỗi: " + ex.getMessage());
        }
    }

    public void show() { frame.setVisible(true); }
}