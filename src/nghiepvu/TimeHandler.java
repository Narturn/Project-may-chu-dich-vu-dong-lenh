package nghiepvu;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * Bộ xử lý các lệnh thời gian hệ thống: TIME và DATE (F06).
 */
public class TimeHandler implements Function<String, String> {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Clock clock;

    public TimeHandler() {
        this(Clock.systemDefaultZone());
    }

    public TimeHandler(Clock clock) {
        if (clock == null) {
            throw new IllegalArgumentException("Clock không được null");
        }
        this.clock = clock;
    }

    /**
     * Trả về giờ hiện tại của hệ thống theo định dạng HH:mm:ss.
     *
     * @param args tham số (bị bỏ qua)
     * @return chuỗi biểu diễn thời gian
     */
    public String handleTime(String args) {
        return LocalTime.now(clock).format(TIME_FMT);
    }

    /**
     * Trả về ngày hiện tại của hệ thống theo định dạng yyyy-MM-dd.
     *
     * @param args tham số (bị bỏ qua)
     * @return chuỗi biểu diễn ngày
     */
    public String handleDate(String args) {
        return LocalDate.now(clock).format(DATE_FMT);
    }

    @Override
    public String apply(String args) {
        return handleTime(args);
    }
}

