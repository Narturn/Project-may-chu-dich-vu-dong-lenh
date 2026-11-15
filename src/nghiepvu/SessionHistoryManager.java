package nghiepvu;

import giaothuc.Request;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Quản lý lịch sử câu lệnh theo từng phiên kết nối của máy khách (F14 - Tính năng mở rộng).
 */
public class SessionHistoryManager {

    private final Deque<Request> history;
    private final int maxEntries;

    public SessionHistoryManager(int maxEntries) {
        this.maxEntries = maxEntries;
        this.history = new ArrayDeque<>();
    }

    /**
     * Ghi nhận một lệnh vừa thực thi vào lịch sử phiên.
     *
     * @param request bản tin yêu cầu
     */
    public void recordCommand(Request request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Lấy danh sách các lệnh thực thi gần nhất.
     *
     * @return danh sách các {@link Request}
     */
    public List<Request> getRecentCommands() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Định dạng lịch sử phiên thành chuỗi hiển thị qua Streams API.
     *
     * @return chuỗi định dạng lịch sử lệnh
     */
    public String formatHistory() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

