package nghiepvu;

import giaothuc.CommandException;
import giaothuc.ErrorCode;
import giaothuc.Request;

import java.util.Optional;
import java.util.function.Function;

/**
 * Bộ điều phối thực thi lệnh thuộc tầng nghiệp vụ.
 * Tiếp nhận {@link Request}, tra cứu handler trong {@link CommandRegistry},
 * thực thi hàm và bắt ngoại lệ để trả về {@link KetQua}.
 */
public class CommandDispatcher {

    private final CommandRegistry registry;
    private final MetricsCollector metrics;

    public CommandDispatcher(CommandRegistry registry, MetricsCollector metrics) {
        if (registry == null) {
            throw new IllegalArgumentException("CommandRegistry không được null");
        }
        if (metrics == null) {
            throw new IllegalArgumentException("MetricsCollector không được null");
        }
        this.registry = registry;
        this.metrics = metrics;
    }

    /**
     * Điều phối thực thi một yêu cầu lệnh và trả về kết quả nội bộ.
     *
     * @param request yêu cầu câu lệnh đã phân tích cú pháp
     * @return {@link KetQua.ThanhCong} khi hoàn thành hoặc {@link KetQua.ThatBai} khi gặp lỗi
     * @throws IllegalArgumentException nếu request là null
     */
    public KetQua dispatch(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Request không được null");
        }

        Optional<Function<String, String>> handlerOpt = registry.getHandler(request.command());
        if (handlerOpt.isEmpty()) {
            return new KetQua.ThatBai(ErrorCode.ERR_UNKNOWN_COMMAND,
                    "Lệnh '" + request.command() + "' không tồn tại trong hệ thống");
        }

        try {
            Function<String, String> handler = handlerOpt.get();
            String rawResult = handler.apply(request.args());
            String resultData = rawResult == null ? "" : rawResult;
            metrics.recordCommand(request.command());
            return new KetQua.ThanhCong(resultData);
        } catch (CommandException e) {
            return new KetQua.ThatBai(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return new KetQua.ThatBai(ErrorCode.ERR_INTERNAL,
                    ErrorCode.ERR_INTERNAL.getDefaultMessage());
        }
    }
}
