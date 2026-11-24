package nghiepvu;

import giaothuc.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("F09 - Kiểm thử lệnh thống kê STATS với Java Streams API")
class StatsHandlerTest {

    private MetricsCollector metrics;
    private StatsHandler statsHandler;

    @BeforeEach
    void setUp() {
        metrics = new MetricsCollector();
        statsHandler = new StatsHandler(metrics);
    }

    @Test
    @DisplayName("Constructor từ chối MetricsCollector null bằng IllegalArgumentException")
    void constructorRejectsNullMetricsCollector() {
        assertThrows(IllegalArgumentException.class, () -> new StatsHandler(null));
    }

    @Test
    @DisplayName("Snapshot rỗng khi mới khởi động trả về Commands=[NONE]")
    void emptyMetricsReturnsNoneForCommands() {
        assertEquals("TotalConnections=0 | ActiveConnections=0 | Commands=[NONE]",
                statsHandler.apply(""));
    }

    @Test
    @DisplayName("Lọc bỏ các lệnh có số lượt thực thi bằng 0 khỏi danh sách Commands")
    void filtersOutCommandsWithZeroCount() {
        MetricsSnapshot snapshotWithZero = new MetricsSnapshot(
                3,
                0,
                Map.of("ECHO", 2, "TIME", 0, "DATE", 0),
                LocalDateTime.now()
        );
        metrics.restoreFromSnapshot(snapshotWithZero);

        assertEquals("TotalConnections=3 | ActiveConnections=0 | Commands=[ECHO=2]",
                statsHandler.apply(""));

        // Nếu tất cả các lệnh trong map đều có tần suất 0 thì hiển thị [NONE]
        MetricsSnapshot allZeros = new MetricsSnapshot(
                1,
                0,
                Map.of("TIME", 0, "DATE", 0),
                LocalDateTime.now()
        );
        metrics.restoreFromSnapshot(allZeros);
        assertEquals("TotalConnections=1 | ActiveConnections=0 | Commands=[NONE]",
                statsHandler.apply(""));
    }

    @Test
    @DisplayName("Sắp xếp các lệnh theo số lượt thực thi giảm dần")
    void sortsCommandsByFrequencyDescending() {
        metrics.recordCommand("TIME");
        metrics.recordCommand("ECHO");
        metrics.recordCommand("ECHO");
        metrics.recordCommand("UPPER");
        metrics.recordCommand("UPPER");
        metrics.recordCommand("UPPER");

        assertEquals("TotalConnections=0 | ActiveConnections=0 | Commands=[UPPER=3, ECHO=2, TIME=1]",
                statsHandler.apply(""));
    }

    @Test
    @DisplayName("Sắp xếp tên lệnh tăng dần theo bảng chữ cái khi có số lượt thực thi bằng nhau")
    void sortsCommandsAlphabeticallyWhenCountsAreEqual() {
        metrics.recordCommand("UPPER");
        metrics.recordCommand("UPPER");
        metrics.recordCommand("DATE");
        metrics.recordCommand("DATE");
        metrics.recordCommand("ECHO");
        metrics.recordCommand("ECHO");
        metrics.recordCommand("TIME");

        assertEquals("TotalConnections=0 | ActiveConnections=0 | Commands=[DATE=2, ECHO=2, UPPER=2, TIME=1]",
                statsHandler.apply(""));
    }

    @Test
    @DisplayName("Hiển thị chính xác totalConnections và activeConnections")
    void displaysAccurateTotalAndActiveConnections() {
        for (int i = 0; i < 5; i++) {
            metrics.recordAcceptedConnection();
        }
        metrics.incrementActiveConnection();
        metrics.incrementActiveConnection();
        metrics.incrementActiveConnection();
        metrics.decrementActiveConnection();
        metrics.recordCommand("ECHO");

        assertEquals("TotalConnections=5 | ActiveConnections=2 | Commands=[ECHO=1]",
                statsHandler.apply(""));
    }

    @Test
    @DisplayName("Bỏ qua tham số thừa hoặc null mà không ném ngoại lệ")
    void ignoresExtraOrNullArguments() {
        metrics.recordAcceptedConnection();
        metrics.recordCommand("TIME");

        String expected = "TotalConnections=1 | ActiveConnections=0 | Commands=[TIME=1]";
        assertEquals(expected, statsHandler.apply(""));
        assertEquals(expected, statsHandler.apply(null));
        assertEquals(expected, statsHandler.apply("   "));
        assertEquals(expected, statsHandler.apply("all"));
        assertEquals(expected, statsHandler.apply("anything extra 123"));
    }

    @Test
    @DisplayName("MetricsCollector.getMetricsSummary() trả về đúng định dạng đồng nhất với StatsHandler")
    void metricsSummaryDelegatesToStatsHandlerFormat() {
        metrics.recordAcceptedConnection();
        metrics.incrementActiveConnection();
        metrics.recordCommand("ECHO");
        metrics.recordCommand("ECHO");
        metrics.recordCommand("DATE");

        String expected = "TotalConnections=1 | ActiveConnections=1 | Commands=[ECHO=2, DATE=1]";
        assertEquals(expected, statsHandler.apply(""));
        assertEquals(expected, metrics.getMetricsSummary());
    }

    @Test
    @DisplayName("Tích hợp qua CommandRegistry và CommandDispatcher; lượt gọi STATS hiện tại được ghi nhận cho lần gọi tiếp theo")
    void integratesWithDispatcherAndIncrementsStatsCounterAfterExecution() {
        CommandRegistry registry = new CommandRegistry();
        registry.register("ECHO", new EchoHandler());
        registry.register("UPPER", new UpperHandler());
        registry.register("STATS", statsHandler);
        CommandDispatcher dispatcher = new CommandDispatcher(registry, metrics);

        metrics.recordAcceptedConnection();
        metrics.incrementActiveConnection();

        dispatcher.dispatch(Request.of("ECHO", "a"));
        dispatcher.dispatch(Request.of("ECHO", "b"));
        dispatcher.dispatch(Request.of("UPPER", "hello"));

        // Lần gọi STATS thứ nhất: hiển thị ECHO=2, UPPER=1 (chưa tính lượt STATS đang chạy)
        KetQua firstStats = dispatcher.dispatch(Request.of("STATS", ""));
        assertInstanceOf(KetQua.ThanhCong.class, firstStats);
        assertEquals("TotalConnections=1 | ActiveConnections=1 | Commands=[ECHO=2, UPPER=1]",
                ((KetQua.ThanhCong) firstStats).duLieu());

        // Lần gọi STATS thứ hai: thấy STATS=1 đã được ghi nhận sau lần gọi thứ nhất
        KetQua secondStats = dispatcher.dispatch(Request.of("STATS", "all"));
        assertInstanceOf(KetQua.ThanhCong.class, secondStats);
        assertEquals("TotalConnections=1 | ActiveConnections=1 | Commands=[ECHO=2, STATS=1, UPPER=1]",
                ((KetQua.ThanhCong) secondStats).duLieu());
    }
}

