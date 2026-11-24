package mang;

import giaothuc.CommandException;
import giaothuc.ErrorCode;
import giaothuc.ProtocolParser;
import giaothuc.Response;
import nghiepvu.CommandDispatcher;
import nghiepvu.KetQua;
import nghiepvu.MetricsCollector;
import nghiepvu.RateLimiter;
import nghiepvu.SessionHistoryManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * Worker F05 đọc lệnh một dòng, gọi nghiệp vụ F04, chuyển KetQua thành Response và gửi qua TCP.
 * Các phụ thuộc thống kê, lịch sử và giới hạn tốc độ được tích hợp tại F08/F14/F15.
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private final ProtocolParser parser;
    private final CommandDispatcher dispatcher;
    private final MetricsCollector metrics;
    private final SessionHistoryManager sessionHistory;
    private final RateLimiter rateLimiter;
    private boolean skipLf;

    public ClientHandler(Socket clientSocket, CommandDispatcher dispatcher, MetricsCollector metrics) {
        this(clientSocket, dispatcher, metrics, null, null);
    }

    public ClientHandler(Socket clientSocket, CommandDispatcher dispatcher, MetricsCollector metrics,
                         SessionHistoryManager sessionHistory, RateLimiter rateLimiter) {
        if (clientSocket == null || dispatcher == null) {
            throw new IllegalArgumentException("Socket và Dispatcher không được null");
        }
        this.clientSocket = clientSocket;
        this.parser = new ProtocolParser();
        this.dispatcher = dispatcher;
        this.metrics = metrics;
        this.sessionHistory = sessionHistory;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void run() {
        if (metrics != null) {
            metrics.incrementActiveConnection();
        }
        try (Socket socket = clientSocket;
             BufferedReader input = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter output = new PrintWriter(new OutputStreamWriter(
                     socket.getOutputStream(), StandardCharsets.UTF_8))) {
            reader = input;
            writer = output;
            while (!socket.isClosed()) {
                String line;
                try {
                    line = readCommandLine();
                } catch (CommandException e) {
                    sendResponse(Response.err(e.getErrorCode(), e.getMessage()));
                    break; // Đóng phiên quá dài thay vì tiếp tục tích lũy hoặc chờ phần còn lại.
                }
                if (line == null) break;
                sendResponse(processCommandLine(line));
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Đóng kết nối nhàn rỗi: " + clientSocket.getRemoteSocketAddress());
        } catch (IOException e) {
            System.err.println("Kết thúc kết nối client: " + e.getMessage());
        } finally {
            if (metrics != null) {
                metrics.decrementActiveConnection();
            }
        }
    }

    /**
     * Đọc có giới hạn trước khi tạo chuỗi lớn; tương thích LF, CRLF và CR như readLine().
     * Trả về dòng cuối khi gặp EOF; EOF không có nội dung trả về null.
     */
    private String readCommandLine() throws IOException {
        StringBuilder line = new StringBuilder();
        int next;
        while ((next = reader.read()) != -1) {
            if (skipLf) {
                skipLf = false;
                if (next == '\n') continue;
            }
            if (next == '\r') {
                skipLf = true;
                return line.toString();
            }
            if (next == '\n') return line.toString();
            if (line.length() == ProtocolParser.MAX_LINE_LENGTH) {
                throw new CommandException(ErrorCode.ERR_SYNTAX,
                        "Dòng lệnh vượt quá giới hạn " + ProtocolParser.MAX_LINE_LENGTH + " ký tự");
            }
            line.append((char) next);
        }
        return line.isEmpty() ? null : line.toString();
    }

    private Response processCommandLine(String rawLine) {
        try {
            return switch (dispatcher.dispatch(parser.parse(rawLine))) {
                case KetQua.ThanhCong result -> Response.ok(result.duLieu());
                case KetQua.ThatBai result -> Response.err(result.maLoi(), result.thongDiep());
            };
        } catch (CommandException e) {
            return Response.err(e.getErrorCode(), e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            return Response.err(ErrorCode.ERR_INTERNAL, ErrorCode.ERR_INTERNAL.getDefaultMessage());
        }
    }

    private void sendResponse(Response response) throws IOException {
        // toProtocolString() đã có newline; không dùng println() để tránh thêm dòng rỗng.
        writer.print(response.toProtocolString());
        writer.flush();
        if (writer.checkError()) throw new IOException("Không thể gửi phản hồi tới client");
    }
}
