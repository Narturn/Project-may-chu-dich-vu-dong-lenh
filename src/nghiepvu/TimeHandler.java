package nghiepvu;

import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * Bộ xử lý các lệnh thời gian hệ thống: TIME và DATE (F06).
 */
public class TimeHandler implements Function<String, String> {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TimeHandler() {
    }

    /**
     * Trả về giờ hiện tại của hệ thống theo định dạng HH:mm:ss.
     *
     * @param args tham số (nếu có)
     * @return chuỗi biểu diễn thời gian
     */
    public String handleTime(String args) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Trả về ngày hiện tại của hệ thống theo định dạng yyyy-MM-dd.
     *
     * @param args tham số (nếu có)
     * @return chuỗi biểu diễn ngày
     */
    public String handleDate(String args) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String apply(String args) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

