package nghiepvu;

import giaothuc.CommandException;
import giaothuc.ErrorCode;
import giaothuc.Request;

/**
 * Bộ điều phối thực thi lệnh thuộc tầng nghiệp vụ.
 * Tiếp nhận {@link Request}, tra cứu handler trong {@link CommandRegistry},
 * thực thi hàm, ghi nhận bộ đếm {@link MetricsCollector}, và bắt ngoại lệ để trả về {@link KetQua}.
 */
public class CommandDispatcher {

    private final CommandRegistry registry;
    private final MetricsCollector metrics;

    public CommandDispatcher(CommandRegistry registry, MetricsCollector metrics) {
        this.registry = registry;
        this.metrics = metrics;
    }

    /**
     * Điều phối thực thi một yêu cầu lệnh và trả về kết quả nội bộ.
     *
     * @param request yêu cầu câu lệnh đã phân tích cú pháp
     * @return {@link KetQua.ThanhCong} khi hoàn thành hoặc {@link KetQua.ThatBai} khi gặp lỗi
     */
    public KetQua dispatch(Request request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

