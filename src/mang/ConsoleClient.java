package mang;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Ứng dụng khách dòng lệnh mẫu (F11), kết nối tới {@link CommandServer} qua TCP Socket
 * và cung cấp vòng lặp tương tác giao tiếp với người dùng trên màn hình Console.
 */
public class ConsoleClient {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8080;

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ConsoleClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Mở kết nối Socket tới máy chủ.
     */
    public void connect() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Gửi một dòng văn bản yêu cầu tới máy chủ và nhận dòng phản hồi.
     *
     * @param rawLine dòng lệnh người dùng nhập
     * @return chuỗi phản hồi từ máy chủ
     */
    public String sendRequest(String rawLine) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Khởi chạy vòng lặp đọc nhập từ bàn phím và in kết quả ra Console cho tới khi gặp QUIT.
     */
    public void startInteractiveLoop() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Đóng socket và các luồng I/O an toàn.
     */
    public void disconnect() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Điểm khởi chạy chính của ứng dụng Máy khách (Client).
     *
     * @param args tham số dòng lệnh (tùy chọn: host và port)
     */
    public static void main(String[] args) {
        System.out.println("Máy khách Dòng lệnh (Interactive Console Client) - Đang khởi động...");
    }
}

