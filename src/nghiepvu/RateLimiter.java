package nghiepvu;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Giới hạn tần suất gọi lệnh của máy khách theo cửa sổ thời gian trượt (F15 - Tính năng mở rộng).
 */
public class RateLimiter {

    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Deque<Long>> clientWindows;

    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.clientWindows = new ConcurrentHashMap<>();
    }

    /**
     * Thử xin quyền gửi yêu cầu cho một định danh máy khách.
     *
     * @param clientKey định danh duy nhất của client (IP hoặc ID kết nối)
     * @return true nếu còn quota trong cửa sổ thời gian; false nếu vượt ngưỡng
     */
    public boolean tryAcquire(String clientKey) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Giải phóng dữ liệu cửa sổ thời gian của client khi ngắt kết nối để chống rò rỉ bộ nhớ.
     *
     * @param clientKey định danh client
     */
    public void evictClient(String clientKey) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

