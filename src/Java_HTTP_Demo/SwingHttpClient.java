package Java_HTTP_Demo;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.*;

public class SwingHttpClient extends JFrame {
    private JTextArea textArea;
    private JTextField idTextField;
    private JTextField usernameTextField;
    private JTextField emailTextField; 

    public SwingHttpClient() {
        setTitle("HUST - Quản lý User (HTTP)");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new GridLayout(3, 2, 10, 10));
        idTextField = new JTextField();
        usernameTextField = new JTextField();
        emailTextField = new JTextField();
        
        fieldPanel.add(new JLabel("id: "));
        fieldPanel.add(idTextField);
        fieldPanel.add(new JLabel("username: "));
        fieldPanel.add(usernameTextField);
        fieldPanel.add(new JLabel("email: "));
        fieldPanel.add(emailTextField);
        
        textArea = new JTextArea();
        JButton btnFetch = new JButton("Lấy danh sách");
        JButton btnOpenAdd = new JButton("Thêm mới...");
        JButton btnOpenUpdate = new JButton("Sửa...");
        JButton btnOpenDelete = new JButton("Xóa...");

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnFetch);
        btnPanel.add(btnOpenAdd);
        btnPanel.add(btnOpenUpdate);
        btnPanel.add(btnOpenDelete);

        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
        add(fieldPanel, BorderLayout.NORTH);

        // Logic lấy dữ liệu
        btnFetch.addActionListener(e -> fetchData());

        // Logic mở cửa sổ thêm
        btnOpenAdd.addActionListener(e -> {
            AddHttpClient addWin = new AddHttpClient(() -> fetchData());
            addWin.show();
        });
        
        btnOpenUpdate.addActionListener(e -> {
        	UpdateHttpClient updateWin = new UpdateHttpClient(() -> fetchData());
        	updateWin.show();
        });
        
        btnOpenDelete.addActionListener(e -> {
        	DeleteHttpClient deleteWin = new DeleteHttpClient(() -> fetchData());
        	deleteWin.show();
        });
        
        fetchData(); // Tự động load dữ liệu khi mở app
    }

    private void fetchData() {
        try {
            // 1. Tạo chuỗi query string từ các TextField
            String query = "id=" + idTextField.getText().trim() +
                           "&username=" + usernameTextField.getText().trim() + 
                           "&email=" + emailTextField.getText().trim();
            
            // 2. Nối query vào URL sau dấu "?"
            String url = "http://localhost:9000/api/users?" + query;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url)) // URL lúc này đã có đầy đủ thông tin lọc
                    .GET() // Không truyền gì vào hàm GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            textArea.setText(response.body());
            
        } catch (Exception ex) { 
            textArea.setText("Lỗi kết nối server: " + ex.getMessage()); 
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingHttpClient().setVisible(true));
    }
}