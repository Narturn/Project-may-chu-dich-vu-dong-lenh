package nghiepvu;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Khung đăng ký và tra cứu các bộ xử lý lệnh theo cấu trúc {@code Map<String, Function<String, String>>}
 * theo đúng yêu cầu cốt lõi của đề tài.
 */
public class CommandRegistry {

    private final Map<String, Function<String, String>> handlers;

    public CommandRegistry() {
        this.handlers = new ConcurrentHashMap<>();
    }

    /**
     * Đăng ký một bộ xử lý lệnh vào registry.
     *
     * @param cmd     tên câu lệnh (không phân biệt chữ hoa thường)
     * @param handler hàm xử lý dạng {@code Function<String, String>}
     */
    public void register(String cmd, Function<String, String> handler) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Tra cứu bộ xử lý theo tên câu lệnh.
     *
     * @param cmd tên câu lệnh cần tra cứu
     * @return {@link Optional} chứa handler nếu tìm thấy, hoặc rỗng nếu chưa đăng ký
     */
    public Optional<Function<String, String>> getHandler(String cmd) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Lấy tập hợp tên tất cả các câu lệnh đã được đăng ký.
     *
     * @return tập hợp tên lệnh
     */
    public Set<String> getSupportedCommands() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

