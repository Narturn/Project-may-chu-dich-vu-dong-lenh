package mang;

import giaothuc.ProtocolParser;
import giaothuc.Response;
import nghiepvu.CommandDispatcher;
import nghiepvu.MetricsCollector;
import nghiepvu.RateLimiter;
import nghiepvu.SessionHistoryManager;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Worker luồng phục vụ xử lý phiên kết nối của một máy khách (F05).
 * Chịu trách nhiệm đọc dòng văn bản từ Socket, chuyển qua {@link ProtocolParser},
 * gọi {@link CommandDispatcher}, chuyển đổi {@link nghiepvu.KetQua} sang {@link Response},
 * và ghi kết quả trả về Socket.
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

    public ClientHandler(Socket clientSocket, CommandDispatcher dispatcher, MetricsCollector metrics) {
        this(clientSocket, dispatcher, metrics, null, null);
    }

    public ClientHandler(Socket clientSocket, CommandDispatcher dispatcher, MetricsCollector metrics,
                         SessionHistoryManager sessionHistory, RateLimiter rateLimiter) {
        this.clientSocket = clientSocket;
        this.parser = new ProtocolParser();
        this.dispatcher = dispatcher;
        this.metrics = metrics;
        this.sessionHistory = sessionHistory;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Xử lý một dòng lệnh thô từ client, thực thi nghiệp vụ và đóng gói Response.
     *
     * @param rawLine dòng văn bản client gửi lên
     * @return đối tượng {@link Response} đóng gói kết quả
     */
    private Response processCommandLine(String rawLine) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Gửi bản tin phản hồi xuống luồng xuất của Socket.
     *
     * @param response bản tin phản hồi
     */
    private void sendResponse(Response response) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

