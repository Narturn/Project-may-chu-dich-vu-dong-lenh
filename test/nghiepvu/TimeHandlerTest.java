package nghiepvu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("F06 - Kiểm thử TimeHandler")
class TimeHandlerTest {

    private static final ZoneId TEST_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    // 2026-11-21T14:05:09+07:00 = 2026-11-21T07:05:09Z
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-11-21T07:05:09Z"), TEST_ZONE);

    @Test
    @DisplayName("handleTime trả về đúng giờ cố định HH:mm:ss với Clock.fixed")
    void returnsExpectedTimeWithFixedClock() {
        TimeHandler handler = new TimeHandler(FIXED_CLOCK);
        assertEquals("14:05:09", handler.handleTime(""));
        assertEquals("14:05:09", handler.apply(""));
    }

    @Test
    @DisplayName("handleDate trả về đúng ngày cố định yyyy-MM-dd với Clock.fixed")
    void returnsExpectedDateWithFixedClock() {
        TimeHandler handler = new TimeHandler(FIXED_CLOCK);
        assertEquals("2026-11-21", handler.handleDate(""));
    }

    @Test
    @DisplayName("TIME bỏ qua các tham số thừa hoặc null mà không báo lỗi")
    void timeIgnoresExtraOrNullArguments() {
        TimeHandler handler = new TimeHandler(FIXED_CLOCK);
        assertEquals("14:05:09", handler.handleTime(null));
        assertEquals("14:05:09", handler.handleTime("   "));
        assertEquals("14:05:09", handler.handleTime("tham so thua 123"));
        assertEquals("14:05:09", handler.apply("tham so thua"));
    }

    @Test
    @DisplayName("DATE bỏ qua các tham số thừa hoặc null mà không báo lỗi")
    void dateIgnoresExtraOrNullArguments() {
        TimeHandler handler = new TimeHandler(FIXED_CLOCK);
        assertEquals("2026-11-21", handler.handleDate(null));
        assertEquals("2026-11-21", handler.handleDate("   "));
        assertEquals("2026-11-21", handler.handleDate("hom nay la ngay may"));
    }

    @Test
    @DisplayName("Định dạng TIME từ constructor mặc định khớp regex ^\\d{2}:\\d{2}:\\d{2}$")
    void defaultClockTimeMatchesExpectedPattern() {
        TimeHandler handler = new TimeHandler();
        String time = handler.handleTime("");
        assertTrue(time.matches("^\\d{2}:\\d{2}:\\d{2}$"),
                "Định dạng TIME không khớp HH:mm:ss: " + time);
    }

    @Test
    @DisplayName("Định dạng DATE từ constructor mặc định khớp regex ^\\d{4}-\\d{2}-\\d{2}$")
    void defaultClockDateMatchesExpectedPattern() {
        TimeHandler handler = new TimeHandler();
        String date = handler.handleDate("");
        assertTrue(date.matches("^\\d{4}-\\d{2}-\\d{2}$"),
                "Định dạng DATE không khớp yyyy-MM-dd: " + date);
    }

    @Test
    @DisplayName("Constructor từ chối Clock null bằng IllegalArgumentException")
    void constructorRejectsNullClock() {
        assertThrows(IllegalArgumentException.class, () -> new TimeHandler(null));
    }
}

