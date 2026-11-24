package nghiepvu;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Bộ xử lý lệnh STATS (F09), tổng hợp dữ liệu thống kê từ {@link MetricsCollector}
 * bằng cách lấy {@link MetricsSnapshot} an toàn luồng và xử lý qua Java Streams API.
 */
public class StatsHandler implements Function<String, String> {

    private final MetricsCollector metrics;

    public StatsHandler(MetricsCollector metrics) {
        if (metrics == null) {
            throw new IllegalArgumentException("MetricsCollector không được null");
        }
        this.metrics = metrics;
    }

    @Override
    public String apply(String args) {
        return formatWithStreams(metrics.getSnapshot());
    }

    /**
     * Định dạng dữ liệu bản sao chụp thành chuỗi hiển thị một dòng qua Streams API.
     *
     * @param snap bản sao chụp dữ liệu thống kê
     * @return chuỗi định dạng thống kê
     */
    private String formatWithStreams(MetricsSnapshot snap) {
        if (snap == null) {
            return "TotalConnections=0 | ActiveConnections=0 | Commands=[NONE]";
        }
        String commandsPart = snap.commandCounts().entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() > 0)
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
        if (commandsPart.isEmpty()) {
            commandsPart = "NONE";
        }
        return "TotalConnections=" + snap.totalConnections()
                + " | ActiveConnections=" + snap.activeConnections()
                + " | Commands=[" + commandsPart + "]";
    }
}

