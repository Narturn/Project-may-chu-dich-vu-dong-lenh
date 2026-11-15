package mang;

import luutru.MetricsRepository;
import nghiepvu.CommandDispatcher;
import nghiepvu.MetricsCollector;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Máy chủ mạng TCP đa luồng (F05), quản lý ServerSocket và phân bổ kết nối cho Worker
 * thông qua {@link ExecutorService} với Bể luồng cố định (Fixed Thread Pool) kết hợp {@code setSoTimeout}.
 */
public class CommandServer {

    public static final int DEFAULT_PORT = 8080;
    public static final int DEFAULT_THREAD_POOL_SIZE = 10;
    public static final int DEFAULT_SO_TIMEOUT_MS = 60_000;

    private final int port;
    private volatile boolean isRunning;
    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final int soTimeoutMillis;
    private final CommandDispatcher dispatcher;
    private final MetricsCollector metrics;
    private final MetricsRepository repository;

    public CommandServer(int port, CommandDispatcher dispatcher, MetricsCollector metrics, MetricsRepository repository) {
        this(port, DEFAULT_THREAD_POOL_SIZE, DEFAULT_SO_TIMEOUT_MS, dispatcher, metrics, repository);
    }

    public CommandServer(int port, int threadPoolSize, int soTimeoutMillis,
                         CommandDispatcher dispatcher, MetricsCollector metrics, MetricsRepository repository) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
        this.soTimeoutMillis = soTimeoutMillis;
        this.dispatcher = dispatcher;
        this.metrics = metrics;
        this.repository = repository;
        this.isRunning = false;
    }

    /**
     * Khởi động máy chủ, mở ServerSocket và lắng nghe các kết nối từ client.
     */
    public void start() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Dừng máy chủ an toàn, đóng ServerSocket, giải phóng thread pool và lưu thống kê ra tệp CSV.
     */
    public void stop() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Đăng ký Shutdown Hook với JVM để đảm bảo dữ liệu được lưu bền vững khi tiến trình dừng đột ngột.
     */
    private void registerShutdownHook() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Điểm khởi chạy chính của ứng dụng Máy chủ (Server).
     *
     * @param args tham số dòng lệnh (tùy chọn: cổng lắng nghe)
     */
    public static void main(String[] args) {
        System.out.println("Máy chủ Dịch vụ Dòng lệnh (Đề tài 31) - Đang khởi động...");
    }
}

