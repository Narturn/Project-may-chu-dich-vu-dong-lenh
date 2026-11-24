package nghiepvu;

import giaothuc.CommandException;
import giaothuc.ErrorCode;
import giaothuc.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("F08 - Kiểm thử bộ đếm thống kê an toàn luồng MetricsCollector")
class MetricsCollectorTest {

    private MetricsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new MetricsCollector();
    }

    @Test
    @DisplayName("Trạng thái khởi tạo ban đầu có tất cả bộ đếm bằng 0 và danh sách lệnh rỗng")
    void initialStateIsZero() {
        MetricsSnapshot snapshot = collector.getSnapshot();

        assertEquals(0, snapshot.totalConnections());
        assertEquals(0, snapshot.activeConnections());
        assertTrue(snapshot.commandCounts().isEmpty());
        assertNotNull(snapshot.capturedAt());
    }

    @Test
    @DisplayName("recordAcceptedConnection chỉ tăng totalConnections; incrementActiveConnection chỉ tăng activeConnections")
    void separatesAcceptedConnectionFromActiveConnectionCounters() {
        collector.recordAcceptedConnection();
        collector.recordAcceptedConnection();

        MetricsSnapshot queuedSnap = collector.getSnapshot();
        assertEquals(2, queuedSnap.totalConnections());
        assertEquals(0, queuedSnap.activeConnections());

        collector.incrementActiveConnection();
        MetricsSnapshot servingSnap = collector.getSnapshot();
        assertEquals(2, servingSnap.totalConnections(), "Khi worker bắt đầu phục vụ không được tăng totalConnections lần 2");
        assertEquals(1, servingSnap.activeConnections());

        collector.decrementActiveConnection();
        MetricsSnapshot finishedSnap = collector.getSnapshot();
        assertEquals(2, finishedSnap.totalConnections());
        assertEquals(0, finishedSnap.activeConnections());
    }

    @Test
    @DisplayName("incrementConnection tăng cả totalConnections và activeConnections; decrementActiveConnection chỉ giảm activeConnections")
    void tracksTotalAndActiveConnections() {
        collector.incrementConnection();
        collector.incrementConnection();
        collector.incrementConnection();
        collector.decrementActiveConnection();

        MetricsSnapshot snapshot = collector.getSnapshot();
        assertEquals(3, snapshot.totalConnections());
        assertEquals(2, snapshot.activeConnections());
    }

    @Test
    @DisplayName("decrementActiveConnection không bao giờ làm activeConnections giảm xuống số âm")
    void activeConnectionsNeverDropsBelowZero() {
        collector.decrementActiveConnection();
        assertEquals(0, collector.getSnapshot().activeConnections());

        collector.incrementConnection();
        collector.decrementActiveConnection();
        collector.decrementActiveConnection();

        MetricsSnapshot snapshot = collector.getSnapshot();
        assertEquals(1, snapshot.totalConnections());
        assertEquals(0, snapshot.activeConnections());
    }

    @Test
    @DisplayName("recordCommand chuẩn hóa tên lệnh về chữ in hoa và cộng dồn chính xác")
    void recordsAndNormalizesCommandNames() {
        collector.recordCommand("echo");
        collector.recordCommand("ECHO");
        collector.recordCommand("  Echo  ");
        collector.recordCommand("time");

        MetricsSnapshot snapshot = collector.getSnapshot();
        assertEquals(2, snapshot.commandCounts().size());
        assertEquals(3, snapshot.commandCounts().get("ECHO"));
        assertEquals(1, snapshot.commandCounts().get("TIME"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t"})
    @DisplayName("recordCommand từ chối tên lệnh null hoặc rỗng bằng IllegalArgumentException")
    void recordCommandRejectsNullOrBlankCommandName(String blankName) {
        assertThrows(IllegalArgumentException.class, () -> collector.recordCommand(null));
        assertThrows(IllegalArgumentException.class, () -> collector.recordCommand(blankName));
    }

    @Test
    @DisplayName("restoreFromSnapshot khôi phục totalConnections, commandCounts và đặt activeConnections về 0")
    void restoresTotalAndCommandsWhileResettingActiveConnectionsToZero() {
        collector.incrementConnection();
        collector.recordCommand("UPPER");

        MetricsSnapshot oldSnapshot = new MetricsSnapshot(
                25,
                4,
                Map.of("echo", 10, "TIME", 7),
                LocalDateTime.of(2026, 11, 20, 12, 0)
        );
        collector.restoreFromSnapshot(oldSnapshot);

        MetricsSnapshot current = collector.getSnapshot();
        assertEquals(25, current.totalConnections());
        assertEquals(0, current.activeConnections(), "activeConnections của phiên cũ không còn tồn tại, phải đặt về 0");
        assertEquals(Map.of("ECHO", 10, "TIME", 7), current.commandCounts());
    }

    @Test
    @DisplayName("restoreFromSnapshot từ chối snapshot null")
    void restoreFromSnapshotRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> collector.restoreFromSnapshot(null));
    }

    @Test
    @DisplayName("Chống tràn số nguyên (Integer.MAX_VALUE): bộ đếm giữ ở ngưỡng bão hòa thay vì tràn sang số âm")
    void preventsIntegerOverflowOnCounters() {
        MetricsSnapshot maxSnapshot = new MetricsSnapshot(
                Integer.MAX_VALUE,
                0,
                Map.of("ECHO", Integer.MAX_VALUE),
                LocalDateTime.now()
        );
        collector.restoreFromSnapshot(maxSnapshot);

        collector.incrementConnection();
        collector.recordCommand("ECHO");

        MetricsSnapshot after = collector.getSnapshot();
        assertEquals(Integer.MAX_VALUE, after.totalConnections());
        assertEquals(1, after.activeConnections());
        assertEquals(Integer.MAX_VALUE, after.commandCounts().get("ECHO"));
    }

    @Test
    @DisplayName("An toàn đa luồng: nhiều worker cập nhật đồng thời không mất lượt đếm và getSnapshot luôn nhất quán")
    void concurrentUpdatesAndSnapshotsRemainConsistentWithoutLostUpdates() throws Exception {
        int workerThreads = 12;
        int iterationsPerThread = 500;
        ExecutorService pool = Executors.newFixedThreadPool(workerThreads + 2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch workersDone = new CountDownLatch(workerThreads);
        AtomicBoolean running = new AtomicBoolean(true);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < workerThreads; i++) {
            futures.add(pool.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < iterationsPerThread; j++) {
                        collector.incrementConnection();
                        collector.recordCommand("ECHO");
                        collector.recordCommand("TIME");
                        collector.decrementActiveConnection();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    workersDone.countDown();
                }
            }));
        }

        // 2 luồng liên tục chụp snapshot trong khi các worker đang cập nhật
        for (int r = 0; r < 2; r++) {
            futures.add(pool.submit(() -> {
                try {
                    startLatch.await();
                    while (running.get()) {
                        MetricsSnapshot snap = collector.getSnapshot();
                        assertTrue(snap.totalConnections() >= 0);
                        assertTrue(snap.activeConnections() >= 0);
                        assertTrue(snap.activeConnections() <= snap.totalConnections(),
                                "activeConnections không bao giờ được vượt quá totalConnections trong snapshot");
                        assertTrue(snap.activeConnections() <= workerThreads,
                                "activeConnections không vượt quá số worker đang mở phiên");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        startLatch.countDown();
        assertTrue(workersDone.await(10, TimeUnit.SECONDS), "Các worker chưa hoàn thành đúng hạn");
        running.set(false);
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));

        for (Future<?> future : futures) {
            future.get();
        }

        int expectedTotal = workerThreads * iterationsPerThread;
        MetricsSnapshot finalSnapshot = collector.getSnapshot();
        assertEquals(expectedTotal, finalSnapshot.totalConnections());
        assertEquals(0, finalSnapshot.activeConnections());
        assertEquals(expectedTotal, finalSnapshot.commandCounts().get("ECHO"));
        assertEquals(expectedTotal, finalSnapshot.commandCounts().get("TIME"));
    }

    @Test
    @DisplayName("Tích hợp với CommandDispatcher: chỉ ghi nhận lệnh khi handler thực thi thành công")
    void commandDispatcherRecordsOnlySuccessfulCommands() {
        CommandRegistry registry = new CommandRegistry();
        registry.register("ECHO", new EchoHandler());
        registry.register("UPPER", new UpperHandler());
        registry.register("BOOM", args -> {
            throw new RuntimeException("Lỗi bất ngờ");
        });
        CommandDispatcher dispatcher = new CommandDispatcher(registry, collector);

        // 2 lệnh thành công
        assertInstanceOf(KetQua.ThanhCong.class, dispatcher.dispatch(Request.of("ECHO", "xin chao")));
        assertInstanceOf(KetQua.ThanhCong.class, dispatcher.dispatch(Request.of("UPPER", "abc")));

        // 1 lệnh không tồn tại + 1 lệnh lỗi tham số + 1 lệnh lỗi nội bộ -> không được tăng bộ đếm
        assertInstanceOf(KetQua.ThatBai.class, dispatcher.dispatch(Request.of("UNKNOWN", "")));
        assertInstanceOf(KetQua.ThatBai.class, dispatcher.dispatch(Request.of("UPPER", "   ")));
        assertInstanceOf(KetQua.ThatBai.class, dispatcher.dispatch(Request.of("BOOM", "")));

        MetricsSnapshot snapshot = collector.getSnapshot();
        assertEquals(Map.of("ECHO", 1, "UPPER", 1), snapshot.commandCounts());
        assertNull(snapshot.commandCounts().get("UNKNOWN"));
        assertNull(snapshot.commandCounts().get("BOOM"));
    }
}

