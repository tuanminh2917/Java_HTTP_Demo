package Java_HTTP_Demo;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DeleteHttpClient {
	private JFrame frame;
	private JTextField idTextField;
	private JButton btn;
	private Runnable onDeleteSuccess;
	
	public DeleteHttpClient(Runnable onDeleteSuccess) {
		this.onDeleteSuccess = onDeleteSuccess;
		frame = new JFrame("Xóa người dùng");
		frame.setSize(300, 100);
		frame.setLayout(new BorderLayout());
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2, 10, 10));
		
		idTextField = new JTextField();
		panel.add(new JLabel("id: "));
		panel.add(idTextField);
		
		btn = new JButton("Xóa");
		
		frame.add(panel, BorderLayout.CENTER);
		frame.add(btn, BorderLayout.SOUTH);
		
		// QUAN TRỌNG: Phải thêm dòng này để nút bấm hoạt động
	    btn.addActionListener(e -> performDelete());
		
	}
	
	private void performDelete() {
		try {
            HttpClient client = HttpClient.newHttpClient();
            
            // BỔ SUNG: thêm action=add vào đây
            String postData = "action=delete" +
                              "&id=" + idTextField.getText();

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
                onDeleteSuccess.run(); 
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Lỗi: " + ex.getMessage());
        }
	}
	
	public void show() { frame.setVisible(true); }
}