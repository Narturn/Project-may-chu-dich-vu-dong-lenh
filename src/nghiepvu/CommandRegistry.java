package nghiepvu;

import java.util.Locale;
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
     * Tên lệnh được chuẩn hóa chữ hoa theo quy ước nội bộ.
     *
     * @param cmd     tên câu lệnh (không được null hoặc rỗng)
     * @param handler hàm xử lý dạng {@code Function<String, String>} (không được null)
     * @throws IllegalArgumentException nếu cmd null/rỗng hoặc handler null
     * @throws IllegalStateException    nếu tên câu lệnh đã được đăng ký trước đó (từ chối ghi đè)
     */
    public void register(String cmd, Function<String, String> handler) {
        if (cmd == null || cmd.isBlank()) {
            throw new IllegalArgumentException("Tên câu lệnh không được null hoặc rỗng");
        }
        if (handler == null) {
            throw new IllegalArgumentException("Handler không được null");
        }

        // Tên lệnh được chuẩn hóa bằng trim().toUpperCase(Locale.ROOT)
        String key = cmd.trim().toUpperCase(Locale.ROOT);
        Function<String, String> existing = handlers.putIfAbsent(key, handler);
        if (existing != null) {
            throw new IllegalStateException("Lệnh '" + key + "' đã được đăng ký trước đó trong hệ thống");
        }
    }

    /**
     * Tra cứu bộ xử lý theo tên câu lệnh.
     *
     * @param cmd tên câu lệnh cần tra cứu
     * @return {@link Optional} chứa handler nếu tìm thấy, hoặc rỗng nếu chưa đăng ký hoặc tên không hợp lệ
     */
    public Optional<Function<String, String>> getHandler(String cmd) {
        if (cmd == null || cmd.isBlank()) {
            return Optional.empty();
        }
        String key = cmd.trim().toUpperCase(Locale.ROOT);
        return Optional.ofNullable(handlers.get(key));
    }

    /**
     * Lấy tập hợp tên tất cả các câu lệnh đã được đăng ký.
     *
     * @return tập hợp tên lệnh bất biến dưới dạng bản chụp (snapshot copy),
     *         không bị thay đổi nếu registry đăng ký thêm lệnh sau đó.
     */
    public Set<String> getSupportedCommands() {
        return Set.copyOf(handlers.keySet());
    }
}
