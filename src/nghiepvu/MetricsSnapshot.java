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
}

