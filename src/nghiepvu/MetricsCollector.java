package nghiepvu;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Quản lý các bộ đếm thống kê an toàn luồng trong bộ nhớ (In-Memory Metrics).
 * Thuộc tầng nghiệp vụ, không trực tiếp thao tác I/O tệp.
 */
public class MetricsCollector {

    private final AtomicInteger totalConnections;
    private final AtomicInteger activeConnections;
    private final ConcurrentHashMap<String, AtomicInteger> commandCounters;

    public MetricsCollector() {
        this.totalConnections = new AtomicInteger(0);
        this.activeConnections = new AtomicInteger(0);
        this.commandCounters = new ConcurrentHashMap<>();
    }

    /**
     * Ghi nhận một kết nối mới đã được máy chủ tiếp nhận (tăng totalConnections).
     * Đồng bộ hóa cùng cơ chế khóa với getSnapshot().
     */
    public synchronized void recordAcceptedConnection() {
        totalConnections.updateAndGet(val -> val == Integer.MAX_VALUE ? Integer.MAX_VALUE : val + 1);
    }

    /**
     * Tăng số kết nối đang hoạt động khi một worker bắt đầu phục vụ client.
     * Đồng bộ hóa cùng cơ chế khóa với getSnapshot().
     */
    public synchronized void incrementActiveConnection() {
        activeConnections.updateAndGet(val -> val == Integer.MAX_VALUE ? Integer.MAX_VALUE : val + 1);
    }

    /**
     * Tăng tổng số kết nối và số kết nối đang hoạt động.
     * Đồng bộ hóa cùng cơ chế khóa với getSnapshot() để đảm bảo tính nhất quán giữa hai bộ đếm.
     */
    public synchronized void incrementConnection() {
        recordAcceptedConnection();
        incrementActiveConnection();
    }

    /**
     * Giảm số kết nối đang hoạt động khi một client ngắt kết nối (không bao giờ giảm dưới 0).
     */
    public synchronized void decrementActiveConnection() {
        activeConnections.updateAndGet(val -> val > 0 ? val - 1 : 0);
    }

    /**
     * Ghi nhận một lượt thực thi lệnh an toàn luồng.
     *
     * @param commandName tên câu lệnh được thực thi
     * @throws IllegalArgumentException nếu tên lệnh null hoặc rỗng
     */
    public synchronized void recordCommand(String commandName) {
        if (commandName == null || commandName.isBlank()) {
            throw new IllegalArgumentException("Tên câu lệnh cần thống kê không được null hoặc rỗng");
        }
        String key = commandName.trim().toUpperCase(Locale.ROOT);
        commandCounters.computeIfAbsent(key, k -> new AtomicInteger(0))
                .updateAndGet(val -> val == Integer.MAX_VALUE ? Integer.MAX_VALUE : val + 1);
    }

    /**
     * Tổng hợp báo cáo thống kê các lệnh bằng Streams API pipeline (F09).
     * Ủy quyền định dạng qua {@link StatsHandler} để tránh lặp lại logic Stream.
     *
     * @return chuỗi định dạng thống kê chi tiết
     */
    public String getMetricsSummary() {
        return new StatsHandler(this).apply("");
    }

    /**
     * Tạo bản sao chụp (snapshot) bất biến và nhất quán từ các bộ đếm in-memory hiện tại.
     *
     * @return đối tượng {@link MetricsSnapshot} an toàn luồng
     */
    public synchronized MetricsSnapshot getSnapshot() {
        Map<String, Integer> countsCopy = new HashMap<>();
        for (Map.Entry<String, AtomicInteger> entry : commandCounters.entrySet()) {
            countsCopy.put(entry.getKey(), entry.getValue().get());
        }
        return new MetricsSnapshot(
                totalConnections.get(),
                activeConnections.get(),
                countsCopy,
                LocalDateTime.now()
        );
    }

    /**
     * Khôi phục trạng thái bộ đếm từ một bản sao chụp (ví dụ nạp từ tệp CSV khi khởi động).
     * Khôi phục totalConnections và commandCounts; đặt activeConnections về 0.
     *
     * @param snapshot bản sao chụp dữ liệu thống kê
     * @throws IllegalArgumentException nếu snapshot là null
     */
    public synchronized void restoreFromSnapshot(MetricsSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("MetricsSnapshot không được null");
        }
        totalConnections.set(snapshot.totalConnections());
        activeConnections.set(0);
        commandCounters.clear();
        for (Map.Entry<String, Integer> entry : snapshot.commandCounts().entrySet()) {
            String key = entry.getKey().trim().toUpperCase(Locale.ROOT);
            commandCounters.put(key, new AtomicInteger(entry.getValue()));
        }
    }
}

