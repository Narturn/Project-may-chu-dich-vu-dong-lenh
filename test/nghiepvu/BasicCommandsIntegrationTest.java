package nghiepvu;

import giaothuc.ErrorCode;
import giaothuc.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("F06, F07 - Kiểm thử tích hợp các lệnh cơ bản qua CommandRegistry và CommandDispatcher")
class BasicCommandsIntegrationTest {

    private static final ZoneId TEST_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    // 2026-11-21T09:30:45+07:00 = 2026-11-21T02:30:45Z
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-11-21T02:30:45Z"), TEST_ZONE);

    private CommandRegistry registry;
    private CommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        registry = new CommandRegistry();
        MetricsCollector metrics = new MetricsCollector();
        TimeHandler timeHandler = new TimeHandler(FIXED_CLOCK);

        registry.register("ECHO", new EchoHandler());
        registry.register("UPPER", new UpperHandler());
        registry.register("TIME", timeHandler::handleTime);
        registry.register("DATE", timeHandler::handleDate);

        dispatcher = new CommandDispatcher(registry, metrics);
    }

    @Test
    @DisplayName("Điều phối lệnh ECHO trả về KetQua.ThanhCong với đúng nội dung tham số")
    void dispatchesEchoCommandSuccessfully() {
        KetQua ketQua = dispatcher.dispatch(Request.of("ECHO", "Xin chào AJP201 \t 123"));
        assertInstanceOf(KetQua.ThanhCong.class, ketQua);
        assertEquals("Xin chào AJP201 \t 123", ((KetQua.ThanhCong) ketQua).duLieu());
    }

    @Test
    @DisplayName("Điều phối lệnh ECHO không có tham số trả về KetQua.ThanhCong với chuỗi rỗng")
    void dispatchesEchoCommandWithEmptyArgs() {
        KetQua ketQua = dispatcher.dispatch(Request.of("ECHO", ""));
        assertInstanceOf(KetQua.ThanhCong.class, ketQua);
        assertEquals("", ((KetQua.ThanhCong) ketQua).duLieu());
    }

    @Test
    @DisplayName("Điều phối lệnh UPPER hợp lệ trả về KetQua.ThanhCong với chuỗi in hoa")
    void dispatchesUpperCommandSuccessfully() {
        KetQua ketQua = dispatcher.dispatch(Request.of("UPPER", "lập trình mạng java 21"));
        assertInstanceOf(KetQua.ThanhCong.class, ketQua);
        assertEquals("LẬP TRÌNH MẠNG JAVA 21", ((KetQua.ThanhCong) ketQua).duLieu());
    }

    @Test
    @DisplayName("Điều phối lệnh UPPER thiếu tham số trả về KetQua.ThatBai với ERR_INVALID_ARGUMENT")
    void dispatchesUpperCommandWithoutArgsReturnsInvalidArgumentError() {
        KetQua ketQuaEmpty = dispatcher.dispatch(Request.of("UPPER", ""));
        assertInstanceOf(KetQua.ThatBai.class, ketQuaEmpty);
        KetQua.ThatBai thatBaiEmpty = (KetQua.ThatBai) ketQuaEmpty;
        assertEquals(ErrorCode.ERR_INVALID_ARGUMENT, thatBaiEmpty.maLoi());
        assertNotNull(thatBaiEmpty.thongDiep());
        assertFalse(thatBaiEmpty.thongDiep().isBlank());

        KetQua ketQuaBlank = dispatcher.dispatch(Request.of("UPPER", "   \t  "));
        assertInstanceOf(KetQua.ThatBai.class, ketQuaBlank);
        assertEquals(ErrorCode.ERR_INVALID_ARGUMENT, ((KetQua.ThatBai) ketQuaBlank).maLoi());
    }

    @Test
    @DisplayName("Điều phối lệnh TIME trả về KetQua.ThanhCong đúng định dạng HH:mm:ss từ Clock cố định")
    void dispatchesTimeCommandSuccessfully() {
        KetQua ketQua = dispatcher.dispatch(Request.of("TIME", ""));
        assertInstanceOf(KetQua.ThanhCong.class, ketQua);
        String timeStr = ((KetQua.ThanhCong) ketQua).duLieu();
        assertEquals("09:30:45", timeStr);
        assertTrue(timeStr.matches("^\\d{2}:\\d{2}:\\d{2}$"));
    }

    @Test
    @DisplayName("Điều phối lệnh DATE trả về KetQua.ThanhCong đúng định dạng yyyy-MM-dd từ Clock cố định")
    void dispatchesDateCommandSuccessfully() {
        KetQua ketQua = dispatcher.dispatch(Request.of("DATE", ""));
        assertInstanceOf(KetQua.ThanhCong.class, ketQua);
        String dateStr = ((KetQua.ThanhCong) ketQua).duLieu();
        assertEquals("2026-11-21", dateStr);
        assertTrue(dateStr.matches("^\\d{4}-\\d{2}-\\d{2}$"));
    }
}

