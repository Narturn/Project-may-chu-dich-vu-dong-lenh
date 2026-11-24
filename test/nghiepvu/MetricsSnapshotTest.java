package nghiepvu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("F08 - Kiểm thử mô hình bất biến MetricsSnapshot")
class MetricsSnapshotTest {

    @Test
    @DisplayName("Khởi tạo MetricsSnapshot hợp lệ giữ đúng dữ liệu")
    void createsValidMetricsSnapshot() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 24, 20, 0, 0);
        Map<String, Integer> counts = Map.of("ECHO", 5, "TIME", 3);

        MetricsSnapshot snapshot = new MetricsSnapshot(10, 2, counts, now);

        assertEquals(10, snapshot.totalConnections());
        assertEquals(2, snapshot.activeConnections());
        assertEquals(counts, snapshot.commandCounts());
        assertEquals(now, snapshot.capturedAt());
    }

    @Test
    @DisplayName("Tự động chuẩn hóa commandCounts null thành Map rỗng và capturedAt null thành thời điểm hiện tại")
    void defaultsNullMapAndNullTimestamp() {
        MetricsSnapshot snapshot = new MetricsSnapshot(0, 0, null, null);

        assertNotNull(snapshot.commandCounts());
        assertTrue(snapshot.commandCounts().isEmpty());
        assertNotNull(snapshot.capturedAt());
    }

    @Test
    @DisplayName("Bản đồ commandCounts trong MetricsSnapshot là bản chụp bất biến (defensive copy)")
    void commandCountsIsImmutableDefensiveCopy() {
        Map<String, Integer> mutable = new HashMap<>();
        mutable.put("ECHO", 1);

        MetricsSnapshot snapshot = new MetricsSnapshot(1, 1, mutable, LocalDateTime.now());
        mutable.put("UPPER", 2);

        assertEquals(1, snapshot.commandCounts().size());
        assertEquals(1, snapshot.commandCounts().get("ECHO"));
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.commandCounts().put("TIME", 3));
    }

    @Test
    @DisplayName("Từ chối totalConnections hoặc activeConnections âm")
    void rejectsNegativeConnectionCounts() {
        assertThrows(IllegalArgumentException.class,
                () -> new MetricsSnapshot(-1, 0, Map.of(), LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class,
                () -> new MetricsSnapshot(0, -1, Map.of(), LocalDateTime.now()));
    }

    @Test
    @DisplayName("Từ chối commandCounts chứa tên lệnh null/rỗng hoặc số lượt thực thi âm/null")
    void rejectsInvalidEntriesInCommandCounts() {
        Map<String, Integer> blankKey = new HashMap<>();
        blankKey.put("   ", 1);
        assertThrows(IllegalArgumentException.class,
                () -> new MetricsSnapshot(1, 0, blankKey, LocalDateTime.now()));

        Map<String, Integer> nullKey = new HashMap<>();
        nullKey.put(null, 1);
        assertThrows(IllegalArgumentException.class,
                () -> new MetricsSnapshot(1, 0, nullKey, LocalDateTime.now()));

        Map<String, Integer> negativeVal = new HashMap<>();
        negativeVal.put("ECHO", -1);
        assertThrows(IllegalArgumentException.class,
                () -> new MetricsSnapshot(1, 0, negativeVal, LocalDateTime.now()));

        Map<String, Integer> nullVal = new HashMap<>();
        nullVal.put("ECHO", null);
        assertThrows(IllegalArgumentException.class,
                () -> new MetricsSnapshot(1, 0, nullVal, LocalDateTime.now()));
    }
}

