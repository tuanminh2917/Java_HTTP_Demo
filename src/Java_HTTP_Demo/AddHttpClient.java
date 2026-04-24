package Java_HTTP_Demo;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.*;

public class AddHttpClient {
    private JFrame frame;
    private JTextField usernameTextField, emailTextField;
    private Runnable onAddSuccess; // Dùng để thông báo cho cửa sổ chính load lại data

    public AddHttpClient(Runnable onAddSuccess) {
        this.onAddSuccess = onAddSuccess;
        frame = new JFrame("Thêm Người Dùng Mới");
        frame.setSize(400, 200);
        frame.setLayout(new GridLayout(3, 2, 10, 10));

        usernameTextField = new JTextField();
        emailTextField = new JTextField();
        JButton btnAdd = new JButton("Thêm vào DB");

        frame.add(new JLabel(" Username:"));
        frame.add(usernameTextField);
        frame.add(new JLabel(" Email:"));
        frame.add(emailTextField);
        frame.add(new JLabel("")); 
        frame.add(btnAdd);

        btnAdd.addActionListener(e -> performAdd());
    }

    private void performAdd() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            // BỔ SUNG: thêm action=add vào đây
            String postData = "action=add" +
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
                onAddSuccess.run(); 
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Lỗi: " + ex.getMessage());
        }
    }

    public void show() { frame.setVisible(true); }
}