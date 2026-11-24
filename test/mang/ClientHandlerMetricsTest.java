package mang;

import nghiepvu.CommandDispatcher;
import nghiepvu.CommandRegistry;
import nghiepvu.EchoHandler;
import nghiepvu.MetricsCollector;
import nghiepvu.MetricsSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(10)
@DisplayName("F08 - Kiểm thử vòng đời bộ đếm kết nối tại ClientHandler và CommandServer")
class ClientHandlerMetricsTest {

    private MetricsCollector metrics;
    private CommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        CommandRegistry registry = new CommandRegistry();
        registry.register("ECHO", new EchoHandler());
        metrics = new MetricsCollector();
        dispatcher = new CommandDispatcher(registry, metrics);
    }

    @Test
    @DisplayName("ClientHandler bắt đầu phục vụ chỉ tăng activeConnections thêm 1, không tăng totalConnections; đóng bình thường giảm activeConnections về 0")
    void clientHandlerIncrementsOnlyActiveOnStartAndDecrementsOnNormalClose() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (ServerSocket listener = new ServerSocket(0);
             Socket clientSide = new Socket("127.0.0.1", listener.getLocalPort());
             Socket serverSide = listener.accept()) {

            Future<?> workerFuture = executor.submit(new ClientHandler(serverSide, dispatcher, metrics));

            PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSide.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSide.getInputStream(), StandardCharsets.UTF_8));

            out.print("ECHO xin chao\n");
            out.flush();
            assertEquals("OK xin chao", in.readLine());

            // Khi đang phục vụ client: activeConnections = 1, totalConnections vẫn = 0 (do không qua CommandServer.accept)
            MetricsSnapshot activeSnap = metrics.getSnapshot();
            assertEquals(0, activeSnap.totalConnections());
            assertEquals(1, activeSnap.activeConnections());
            assertEquals(1, activeSnap.commandCounts().get("ECHO"));

            // Đóng kết nối bình thường (EOF)
            clientSide.close();
            workerFuture.get(3, TimeUnit.SECONDS);

            MetricsSnapshot afterClose = metrics.getSnapshot();
            assertEquals(0, afterClose.totalConnections());
            assertEquals(0, afterClose.activeConnections());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Client ngắt kết nối đột ngột (RST / IOException) vẫn giảm activeConnections trong finally và không bao giờ âm")
    void clientHandlerDecrementsActiveConnectionInFinallyOnAbruptDisconnect() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (ServerSocket listener = new ServerSocket(0);
             Socket clientSide = new Socket("127.0.0.1", listener.getLocalPort());
             Socket serverSide = listener.accept()) {

            Future<?> workerFuture = executor.submit(new ClientHandler(serverSide, dispatcher, metrics));

            PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSide.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSide.getInputStream(), StandardCharsets.UTF_8));

            out.print("ECHO ket noi\n");
            out.flush();
            assertEquals("OK ket noi", in.readLine());
            assertEquals(1, metrics.getSnapshot().activeConnections());

            // Ngắt kết nối bất thường bằng TCP RST
            clientSide.setSoLinger(true, 0);
            clientSide.close();

            workerFuture.get(3, TimeUnit.SECONDS);

            MetricsSnapshot afterAbrupt = metrics.getSnapshot();
            assertEquals(0, afterAbrupt.activeConnections(), "activeConnections phải giảm về 0 trong khối finally");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("CommandServer ghi nhận totalConnections ngay khi accept (kể cả khi chờ trong hàng đợi) và không tăng lần 2 khi worker bắt đầu xử lý")
    void serverRecordsTotalOnAcceptAndActiveOnlyWhenWorkerRuns() throws Exception {
        // Bể luồng cố định chỉ 1 worker để kiểm chứng trạng thái client thứ 2 nằm trong hàng đợi
        CommandServer server = new CommandServer(0, 1, 5000, dispatcher, metrics, null);
        ExecutorService serverRunner = Executors.newSingleThreadExecutor();
        Future<?> serverTask = serverRunner.submit(server::start);

        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (server.getPort() == 0 && System.nanoTime() < deadline) {
                Thread.sleep(10);
            }
            int port = server.getPort();
            assertNotEquals(0, port);

            try (Socket client1 = new Socket("127.0.0.1", port)) {
                PrintWriter out1 = new PrintWriter(new OutputStreamWriter(client1.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader in1 = new BufferedReader(new InputStreamReader(client1.getInputStream(), StandardCharsets.UTF_8));

                out1.print("ECHO client1\n");
                out1.flush();
                assertEquals("OK client1", in1.readLine());

                // Client 1 đang được phục vụ bởi worker duy nhất
                MetricsSnapshot snap1 = metrics.getSnapshot();
                assertEquals(1, snap1.totalConnections());
                assertEquals(1, snap1.activeConnections());

                // Client 2 kết nối: Server accept ngay nhưng phải nằm chờ trong hàng đợi của ExecutorService
                try (Socket client2 = new Socket("127.0.0.1", port)) {
                    long waitAccept = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
                    while (metrics.getSnapshot().totalConnections() < 2 && System.nanoTime() < waitAccept) {
                        Thread.sleep(10);
                    }

                    MetricsSnapshot snapQueued = metrics.getSnapshot();
                    assertEquals(2, snapQueued.totalConnections(),
                            "Client 2 đã được accept nên totalConnections phải tăng lên 2 ngay cả khi đang chờ worker");
                    assertEquals(1, snapQueued.activeConnections(),
                            "Chỉ có 1 worker đang chạy nên activeConnections vẫn bằng 1");

                    // Đóng Client 1 để giải phóng worker cho Client 2
                    client1.close();

                    PrintWriter out2 = new PrintWriter(new OutputStreamWriter(client2.getOutputStream(), StandardCharsets.UTF_8), true);
                    BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream(), StandardCharsets.UTF_8));
                    out2.print("ECHO client2\n");
                    out2.flush();
                    assertEquals("OK client2", in2.readLine());

                    MetricsSnapshot snapClient2Serving = metrics.getSnapshot();
                    assertEquals(2, snapClient2Serving.totalConnections(),
                            "totalConnections không được tăng lần thứ hai khi worker bắt đầu phục vụ Client 2");
                    assertEquals(1, snapClient2Serving.activeConnections());
                }
            }

            // Sau khi cả 2 client đóng kết nối, đợi worker hoàn tất finally
            long waitClose = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (metrics.getSnapshot().activeConnections() > 0 && System.nanoTime() < waitClose) {
                Thread.sleep(10);
            }

            MetricsSnapshot finalSnap = metrics.getSnapshot();
            assertEquals(2, finalSnap.totalConnections());
            assertEquals(0, finalSnap.activeConnections());
            assertEquals(2, finalSnap.commandCounts().get("ECHO"));
        } finally {
            server.stop();
            try {
                serverTask.get(3, TimeUnit.SECONDS);
            } finally {
                serverRunner.shutdownNow();
            }
        }
    }
}

