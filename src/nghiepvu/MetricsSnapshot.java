package nghiepvu;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Bản sao chụp (snapshot) bất biến chứa số liệu thống kê tại một thời điểm,
 * dùng để trao đổi an toàn giữa tầng nghiệp vụ {@link MetricsCollector} và tầng lưu trữ.
 *
 * @param totalConnections  tổng số kết nối đã tiếp nhận từ khi máy chủ khởi động
 * @param activeConnections số kết nối máy khách đang hoạt động đồng thời
 * @param commandCounts     bản đồ thống kê số lượt thực thi theo từng loại lệnh
 * @param capturedAt        thời điểm tạo bản sao chụp
 */
public record MetricsSnapshot(
        int totalConnections,
        int activeConnections,
        Map<String, Integer> commandCounts,
        LocalDateTime capturedAt
) {

    /**
     * Compact constructor kiểm tra tính hợp lệ và đảm bảo tính bất biến của bản sao chụp.
     */
    public MetricsSnapshot {
        if (totalConnections < 0) {
            throw new IllegalArgumentException("Tổng số kết nối không được âm");
        }
        if (activeConnections < 0) {
            throw new IllegalArgumentException("Số kết nối đang hoạt động không được âm");
        }
        if (commandCounts == null) {
            commandCounts = Map.of();
        } else {
            for (Map.Entry<String, Integer> entry : commandCounts.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                if (key == null || key.isBlank()) {
                    throw new IllegalArgumentException("Tên lệnh trong thống kê không được null hoặc rỗng");
                }
                if (value == null || value < 0) {
                    throw new IllegalArgumentException("Số lượt thực thi lệnh không được null hoặc âm");
                }
            }
            commandCounts = Map.copyOf(commandCounts);
        }
        if (capturedAt == null) {
            capturedAt = LocalDateTime.now();
        }
    }
}

