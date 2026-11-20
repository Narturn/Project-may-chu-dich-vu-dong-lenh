package mang;

import giaothuc.CommandException;
import giaothuc.ErrorCode;
import giaothuc.ProtocolParser;
import nghiepvu.CommandDispatcher;
import nghiepvu.CommandRegistry;
import nghiepvu.MetricsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(15)
class CommandServerTest {
    private CommandRegistry registry;
    private MetricsCollector metrics;
    private CommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        registry = new CommandRegistry();
        metrics = new MetricsCollector();
        dispatcher = new CommandDispatcher(registry, metrics);
        // Các Lambda chỉ là dữ liệu kiểm thử F05, không thay thế handler F06/F07.
        registry.register("ECHO", args -> args);
        registry.register("MULTILINE", args -> "một\r\nhai");
        registry.register("BUG", args -> { throw new IllegalStateException("Lỗi giả lập"); });
        registry.register("BAD", args -> {
            throw new CommandException(ErrorCode.ERR_INVALID_ARGUMENT, "Sai tham số");
        });
    }

    @Test
    void servesSeveralUtf8CommandsOnOneConnection() throws Exception {
        try (RunningServer server = start(2, 5000); Peer client = server.connect()) {
            assertEquals("OK Xin chào Việt Nam", client.request("  echo Xin chào Việt Nam  "));
            assertEquals("OK", client.request("ECHO"));
            assertEquals("OK một\\nhai", client.request("MULTILINE"));
            assertEquals("OK vẫn đồng bộ", client.request("ECHO vẫn đồng bộ"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " \t ", "UNKNOWN", "BUG", "BAD"})
    void returnsErrorsAndKeepsConnectionUsable(String line) throws Exception {
        try (RunningServer server = start(2, 5000); Peer client = server.connect()) {
            String reply = client.request(line);
            String code = switch (line) {
                case "UNKNOWN" -> "ERR_UNKNOWN_COMMAND";
                case "BUG" -> "ERR_INTERNAL";
                case "BAD" -> "ERR_INVALID_ARGUMENT";
                default -> "ERR_SYNTAX";
            };
            assertTrue(reply.startsWith("ERR " + code + " "), reply);
            assertEquals("OK tiếp tục", client.request("ECHO tiếp tục"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"\n", "\r\n", "\r"})
    void readsConsecutiveCommandsWithDifferentLineTerminators(String terminator) throws Exception {
        try (RunningServer server = start(2, 5000); Peer client = server.connect()) {
            client.send("ECHO một" + terminator + "ECHO hai" + terminator);
            assertEquals("OK một", client.read());
            assertEquals("OK hai", client.read());
        }
    }

    @Test
    void acceptsMaximumLengthLine() throws Exception {
        String arguments = "x".repeat(ProtocolParser.MAX_LINE_LENGTH - 5);
        try (RunningServer server = start(1, 5000); Peer client = server.connect()) {
            assertEquals("OK " + arguments, client.request("ECHO " + arguments));
        }
    }

    @Test
    void rejectsOversizedInputWithoutWaitingForNewline() throws Exception {
        try (RunningServer server = start(1, 5000); Peer client = server.connect()) {
            client.send("x".repeat(ProtocolParser.MAX_LINE_LENGTH + 1));
            assertTrue(client.read().startsWith("ERR ERR_SYNTAX "));
            assertNull(client.read(), "Đóng phiên quá dài, không tiếp tục giữ dữ liệu trong bộ nhớ");
            try (Peer next = server.connect()) {
                assertEquals("OK bình thường", next.request("ECHO bình thường"));
            }
        }
    }

    @Test
    void processesFinalLineBeforePeerClosesItsOutput() throws Exception {
        try (RunningServer server = start(1, 5000); Peer client = server.connect()) {
            client.send("ECHO cuối");
            client.socket.shutdownOutput();
            assertEquals("OK cuối", client.read());
            assertNull(client.read());
        }
    }

    @Test
    void servesFiveClientsAtTheSameTime() throws Exception {
        CountDownLatch entered = new CountDownLatch(5);
        CountDownLatch release = new CountDownLatch(1);
        registry.register("WAIT", args -> {
            entered.countDown();
            try {
                if (!release.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Hết thời gian chờ");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            return args;
        });
        try (RunningServer server = start(5, 5000);
             ExecutorService clients = Executors.newFixedThreadPool(5)) {
            List<Future<String>> replies = new ArrayList<>();
            try {
                for (int i = 0; i < 5; i++) {
                    int id = i;
                    replies.add(clients.submit(() -> {
                        try (Peer client = server.connect()) {
                            return client.request("WAIT " + id);
                        }
                    }));
                }
                assertTrue(entered.await(3, TimeUnit.SECONDS), "Cả 5 handler phải cùng được chạy");
            } finally {
                release.countDown();
            }
            for (int i = 0; i < 5; i++) assertEquals("OK " + i, replies.get(i).get(3, TimeUnit.SECONDS));
        }
    }

    @Test
    void queuesClientsWhenFixedPoolIsBusy() throws Exception {
        try (RunningServer server = start(1, 5000); Peer first = server.connect()) {
            assertEquals("OK giữ phiên", first.request("ECHO giữ phiên"));
            try (Peer queued = server.connect()) {
                queued.send("ECHO chờ\n");
                queued.socket.setSoTimeout(150);
                assertThrows(SocketTimeoutException.class, queued::read);
                first.close();
                queued.socket.setSoTimeout(4000);
                assertEquals("OK chờ", queued.read());
            }
        }
    }

    @Test
    void refusesExcessConnectionsInsteadOfGrowingQueueForever() throws Exception {
        try (RunningServer server = start(1, 5000); Peer first = server.connect()) {
            assertEquals("OK giữ phiên", first.request("ECHO giữ phiên"));
            try (Peer queued = server.connect(); Peer excess = server.connect()) {
                queued.send("ECHO đã chờ\n");
                assertTrue(excess.read().startsWith("ERR ERR_INTERNAL "));
                assertNull(excess.read());
                first.close();
                assertEquals("OK đã chờ", queued.read());
            }
        }
    }

    @Test
    void idleTimeoutReleasesWorkerForQueuedClient() throws Exception {
        try (RunningServer server = start(1, 300); Peer idle = server.connect()) {
            assertEquals("OK bắt đầu", idle.request("ECHO bắt đầu"));
            try (Peer queued = server.connect()) {
                queued.send("ECHO tiếp theo\n");
                assertNull(idle.read());
                assertEquals("OK tiếp theo", queued.read());
            }
        }
    }

    @Test
    void abruptClientDisconnectDoesNotStopServer() throws Exception {
        try (RunningServer server = start(1, 5000)) {
            try (Peer broken = server.connect()) {
                assertEquals("OK đã nối", broken.request("ECHO đã nối"));
                broken.socket.setSoLinger(true, 0);
            }
            try (Peer healthy = server.connect()) {
                assertEquals("OK vẫn chạy", healthy.request("ECHO vẫn chạy"));
            }
        }
    }

    @Test
    void stopClosesActiveAndQueuedSocketsAndReleasesPort() throws Exception {
        try (RunningServer server = start(1, 5000); Peer active = server.connect()) {
            assertEquals("OK đã nối", active.request("ECHO đã nối"));
            try (Peer queued = server.connect()) {
                queued.send("ECHO đợi\n");
                server.server.stop();
                server.task.get(3, TimeUnit.SECONDS);
                assertNull(active.read());
                // Socket có dữ liệu chưa đọc có thể nhận EOF hoặc RST khi server đóng.
                try { assertNull(queued.read()); } catch (SocketException expected) { }
                assertDoesNotThrow(server.server::stop);
                assertThrows(ConnectException.class, () -> new Socket("127.0.0.1", server.port));
            }
        }
    }

    @Test
    void reportsOccupiedPortWithoutLeakingServerResources() throws Exception {
        try (ServerSocket occupied = new ServerSocket(0)) {
            CommandServer server = new CommandServer(occupied.getLocalPort(), dispatcher, metrics, null);
            UncheckedIOException error = assertThrows(UncheckedIOException.class, server::start);
            assertInstanceOf(BindException.class, error.getCause());
            assertDoesNotThrow(server::stop);
        }
    }

    @Test
    void validatesConfigurationBeforeStartingThreads() {
        assertThrows(IllegalArgumentException.class, () -> new CommandServer(-1, dispatcher, metrics, null));
        assertThrows(IllegalArgumentException.class, () -> new CommandServer(65536, dispatcher, metrics, null));
        assertThrows(IllegalArgumentException.class, () -> new CommandServer(0, 0, 100, dispatcher, metrics, null));
        assertThrows(IllegalArgumentException.class, () -> new CommandServer(0, 1, 0, dispatcher, metrics, null));
        assertThrows(IllegalArgumentException.class, () -> new CommandServer(0, null, metrics, null));
    }

    @Test
    void refusesRestartingSameServerInstance() throws Exception {
        try (RunningServer server = start(1, 5000)) {
            assertThrows(IllegalStateException.class, server.server::start);
            try (Peer client = server.connect()) {
                assertEquals("OK chưa bị dừng", client.request("ECHO chưa bị dừng"));
            }
        }
    }

    @Test
    void jvmShutdownInvokesStopAndClosesConnections(@TempDir Path directory) throws Exception {
        Path ready = directory.resolve("ready.txt");
        Path stopped = directory.resolve("stopped.txt");
        Path log = directory.resolve("process.log");
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classPath = Path.of(CommandServer.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        Process process = new ProcessBuilder(java, "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8", "-cp", classPath, ShutdownProcess.class.getName(), directory.toString())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!Files.exists(ready) && process.isAlive() && System.nanoTime() < deadline) Thread.sleep(10);
            assertTrue(Files.exists(ready), () -> "JVM con chưa khởi động: " + process.info());
            int port = Integer.parseInt(Files.readString(ready));
            try (Peer client = new Peer(port)) {
                assertEquals("OK đã nối", client.request("ECHO đã nối"));
                process.getOutputStream().write(1);
                process.getOutputStream().flush();
                assertTrue(process.waitFor(5, TimeUnit.SECONDS), "Shutdown hook bị treo");
                assertEquals(0, process.exitValue(), Files.readString(log));
                assertEquals("hook completed", Files.readString(stopped));
                assertNull(client.read());
            }
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                assertTrue(process.waitFor(3, TimeUnit.SECONDS));
            }
        }
    }

    /** JVM riêng để kiểm tra System.exit thực sự gọi shutdown hook của server. */
    public static class ShutdownProcess {
        public static void main(String[] args) throws Exception {
            Path directory = Path.of(args[0]);
            CommandRegistry registry = new CommandRegistry();
            registry.register("ECHO", value -> value);
            MetricsCollector metrics = new MetricsCollector();
            CommandServer server = new CommandServer(0, 1, 5000, new CommandDispatcher(registry, metrics), metrics, null) {
                @Override
                public synchronized void stop() {
                    super.stop();
                    if (Thread.currentThread().getName().equals("command-server-shutdown")) {
                        try {
                            Files.writeString(directory.resolve("stopped.txt"), "hook completed");
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                }
            };
            new Thread(server::start).start();
            while (server.getPort() == 0) Thread.sleep(10);
            Files.writeString(directory.resolve("ready.txt"), Integer.toString(server.getPort()));
            System.in.read();
            System.exit(0);
        }
    }
    private RunningServer start(int workers, int timeout) throws Exception {
        return new RunningServer(new CommandServer(0, workers, timeout, dispatcher, metrics, null));
    }

    private static final class RunningServer implements AutoCloseable {
        final CommandServer server;
        final ExecutorService runner = Executors.newSingleThreadExecutor();
        final Future<?> task;
        final int port;

        RunningServer(CommandServer server) throws Exception {
            this.server = server;
            task = runner.submit(server::start);
            try {
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
                while (server.getPort() == 0 && !task.isDone() && System.nanoTime() < deadline) Thread.sleep(10);
                if (task.isDone()) task.get();
                port = server.getPort();
                assertNotEquals(0, port, "Server chưa mở cổng");
            } catch (Exception | AssertionError e) {
                close();
                throw e;
            }
        }

        Peer connect() throws IOException { return new Peer(port); }

        @Override
        public void close() throws Exception {
            server.stop();
            try { task.get(3, TimeUnit.SECONDS); }
            finally { runner.shutdownNow(); }
        }
    }

    private static final class Peer implements AutoCloseable {
        final Socket socket;
        final BufferedReader reader;
        final Writer writer;

        Peer(int port) throws IOException {
            socket = new Socket("127.0.0.1", port);
            socket.setSoTimeout(4000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
        }

        void send(String text) throws IOException { writer.write(text); writer.flush(); }
        String read() throws IOException { return reader.readLine(); }
        String request(String line) throws IOException { send(line + "\n"); return read(); }
        @Override
        public void close() throws IOException { socket.close(); }
    }
}
