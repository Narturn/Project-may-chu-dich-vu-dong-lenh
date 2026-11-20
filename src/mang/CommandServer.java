package mang;

import giaothuc.ErrorCode;
import giaothuc.Response;
import luutru.MetricsRepository;
import nghiepvu.CommandDispatcher;
import nghiepvu.CommandRegistry;
import nghiepvu.EchoHandler;
import nghiepvu.MetricsCollector;
import nghiepvu.TimeHandler;
import nghiepvu.UpperHandler;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Máy chủ TCP đa luồng F05: bể luồng cố định, timeout đọc và đóng các phiên khi dừng.
 * Thống kê kết nối và lưu CSV được tích hợp ở F08/F12 qua các phụ thuộc đã khai báo.
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
    private final Set<Socket> clientSockets = ConcurrentHashMap.newKeySet();
    // Tối đa n phiên đang chạy và n phiên chờ; tránh hàng đợi không giới hạn của fixed pool.
    private final long maxConnections;
    private boolean started;
    private boolean stopped;
    private Thread shutdownHook;

    public CommandServer(int port, CommandDispatcher dispatcher, MetricsCollector metrics, MetricsRepository repository) {
        this(port, DEFAULT_THREAD_POOL_SIZE, DEFAULT_SO_TIMEOUT_MS, dispatcher, metrics, repository);
    }

    public CommandServer(int port, int threadPoolSize, int soTimeoutMillis,
                         CommandDispatcher dispatcher, MetricsCollector metrics, MetricsRepository repository) {
        if (port < 0 || port > 65535) throw new IllegalArgumentException("Cổng phải nằm trong khoảng 0–65535");
        if (threadPoolSize <= 0) throw new IllegalArgumentException("Số worker phải lớn hơn 0");
        if (soTimeoutMillis <= 0) throw new IllegalArgumentException("Timeout phải lớn hơn 0");
        if (dispatcher == null || metrics == null) {
            throw new IllegalArgumentException("Dispatcher và MetricsCollector không được null");
        }
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
        this.soTimeoutMillis = soTimeoutMillis;
        this.dispatcher = dispatcher;
        this.metrics = metrics;
        this.repository = repository;
        this.maxConnections = 2L * threadPoolSize;
    }

    /**
     * Chạy vòng lặp accept trên luồng gọi. Mỗi instance chỉ khởi động một lần.
     *
     * @throws UncheckedIOException nếu không mở được cổng hoặc gặp lỗi lắng nghe
     */
    public void start() {
        synchronized (this) {
            if (started || stopped) throw new IllegalStateException("Server đã khởi động hoặc đã dừng");
            started = true;
        }
        try (ServerSocket listener = new ServerSocket()) {
            synchronized (this) {
                if (stopped) return;
                listener.bind(new InetSocketAddress(port));
                serverSocket = listener;
                isRunning = true;
                registerShutdownHook();
            }
            System.out.println("Máy chủ đang lắng nghe cổng " + listener.getLocalPort());
            while (isRunning) {
                Socket client = listener.accept();
                try {
                    acceptClient(client);
                } catch (IOException | RejectedExecutionException e) {
                    closeResource(client);
                    if (isRunning) System.err.println("Không thể phục vụ client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            // Đóng ServerSocket trong stop() sẽ đánh thức accept() bằng SocketException.
            if (isRunning || !isStopped()) throw new UncheckedIOException("Không thể chạy máy chủ", e);
        } finally {
            stop();
        }
    }

    private synchronized void acceptClient(Socket client) throws IOException {
        if (!isRunning) {
            closeResource(client);
            return;
        }
        if (clientSockets.size() >= maxConnections) {
            try (Socket refused = client;
                 PrintWriter output = new PrintWriter(new OutputStreamWriter(
                         refused.getOutputStream(), StandardCharsets.UTF_8))) {
                output.print(Response.err(ErrorCode.ERR_INTERNAL, "Máy chủ đang bận, vui lòng kết nối lại").toProtocolString());
                output.flush();
            }
            return;
        }
        client.setSoTimeout(soTimeoutMillis);
        clientSockets.add(client);
        try {
            threadPool.execute(() -> {
                try {
                    new ClientHandler(client, dispatcher, metrics).run();
                } finally {
                    clientSockets.remove(client);
                }
            });
        } catch (RejectedExecutionException e) {
            clientSockets.remove(client);
            throw e;
        }
    }

    /** Cổng thực tế, hỗ trợ cổng 0 để hệ điều hành chọn cổng trống khi kiểm thử. */
    public synchronized int getPort() {
        return serverSocket == null ? port : serverSocket.getLocalPort();
    }

    private synchronized boolean isStopped() {
        return stopped;
    }

    /**
     * Ngừng nhận kết nối, đóng cả socket đang chạy/chờ và đợi worker kết thúc.
     * F12 sẽ lưu snapshot sau bước đợi worker này; F05 chưa gọi repository chưa triển khai.
     */
    public synchronized void stop() {
        if (stopped) return;
        stopped = true;
        isRunning = false;
        if (serverSocket != null) closeResource(serverSocket);
        threadPool.shutdown();
        // Interrupt đơn thuần không đánh thức Socket.read(); phải đóng socket.
        for (Socket client : clientSockets) closeResource(client);
        try {
            if (!threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
                if (!threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
                    System.err.println("Worker chưa kết thúc sau thời hạn dừng máy chủ");
                }
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            clientSockets.clear();
            if (shutdownHook != null && Thread.currentThread() != shutdownHook) {
                try {
                    Runtime.getRuntime().removeShutdownHook(shutdownHook);
                } catch (IllegalStateException expected) {
                    // JVM đã bắt đầu shutdown, không còn được phép gỡ hook.
                }
            }
        }
    }

    /** Hook xử lý dừng thông thường của JVM, không bảo đảm chạy khi bị kill cưỡng bức. */
    private void registerShutdownHook() {
        shutdownHook = new Thread(this::stop, "command-server-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private static void closeResource(Closeable resource) {
        try {
            resource.close();
        } catch (IOException e) {
            System.err.println("Lỗi đóng tài nguyên mạng: " + e.getMessage());
        }
    }

    /** Khởi chạy cổng 8080 hoặc cổng được truyền vào; các handler được đăng ký ở F06/F07/F09. */
    public static void main(String[] args) {
        if (args.length > 1) throw new IllegalArgumentException("Cú pháp: CommandServer [port]");
        int port = args.length == 0 ? DEFAULT_PORT : Integer.parseInt(args[0]);
        MetricsCollector metrics = new MetricsCollector();
        CommandRegistry registry = new CommandRegistry();
        TimeHandler timeHandler = new TimeHandler();
        registry.register("ECHO", new EchoHandler());
        registry.register("UPPER", new UpperHandler());
        registry.register("TIME", timeHandler::handleTime);
        registry.register("DATE", timeHandler::handleDate);
        CommandDispatcher dispatcher = new CommandDispatcher(registry, metrics);
        new CommandServer(port, dispatcher, metrics, null).start();
    }
}
