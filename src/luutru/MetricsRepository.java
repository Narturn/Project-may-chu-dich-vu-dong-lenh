package luutru;

import nghiepvu.MetricsSnapshot;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Tầng lưu trữ chịu trách nhiệm đọc/ghi dữ liệu thống kê ra tệp CSV (F12).
 * Chỉ giao tiếp với tầng nghiệp vụ thông qua {@link MetricsSnapshot}, sử dụng {@code try-with-resources}.
 */
public class MetricsRepository {

    public static final String CSV_HEADER = "MetricType,Key,Value,LastUpdated";

    private final Path csvPath;

    public MetricsRepository(Path csvPath) {
        this.csvPath = csvPath;
    }

    /**
     * Lưu bản sao chụp số liệu thống kê ra tệp CSV bằng try-with-resources.
     *
     * @param snapshot bản sao chụp dữ liệu thống kê
     */
    public void save(MetricsSnapshot snapshot) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Nạp dữ liệu thống kê lịch sử từ tệp CSV bằng try-with-resources.
     *
     * @return {@link Optional} chứa {@link MetricsSnapshot} nếu tệp tồn tại và hợp lệ, hoặc rỗng nếu chưa có tệp
     */
    public Optional<MetricsSnapshot> load() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Lấy đường dẫn tệp CSV lưu trữ.
     *
     * @return đường dẫn {@link Path}
     */
    public Path getCsvPath() {
        return csvPath;
    }
}

