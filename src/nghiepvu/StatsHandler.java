package nghiepvu;

import java.util.function.Function;

/**
 * Bộ xử lý lệnh STATS (F09), tổng hợp dữ liệu thống kê từ {@link MetricsCollector}
 * bằng cách lấy {@link MetricsSnapshot} an toàn luồng và xử lý qua Java Streams API.
 */
public class StatsHandler implements Function<String, String> {

    private final MetricsCollector metrics;

    public StatsHandler(MetricsCollector metrics) {
        this.metrics = metrics;
    }

    @Override
    public String apply(String args) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Định dạng dữ liệu bản sao chụp thành chuỗi hiển thị qua Streams API.
     *
     * @param snap bản sao chụp dữ liệu thống kê
     * @return chuỗi định dạng thống kê
     */
    private String formatWithStreams(MetricsSnapshot snap) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

