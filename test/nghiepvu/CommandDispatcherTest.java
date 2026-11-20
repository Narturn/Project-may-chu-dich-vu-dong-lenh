package nghiepvu;

import giaothuc.CommandException;
import giaothuc.ErrorCode;
import giaothuc.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("F04 - Kiểm thử CommandDispatcher")
class CommandDispatcherTest {

    private CommandRegistry registry;
    private MetricsCollector metrics;
    private CommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        registry = new CommandRegistry();
        metrics = new MetricsCollector();
        dispatcher = new CommandDispatcher(registry, metrics);
    }

    @Nested
    @DisplayName("Khởi tạo CommandDispatcher")
    class ConstructorValidation {

        @Test
        @DisplayName("Khởi tạo với registry null ném IllegalArgumentException")
        void nullRegistryThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new CommandDispatcher(null, metrics),
                    "CommandRegistry không được null");
        }

        @Test
        @DisplayName("Khởi tạo với metrics null ném IllegalArgumentException")
        void nullMetricsThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new CommandDispatcher(registry, null),
                    "MetricsCollector không được null");
        }
    }

    @Nested
    @DisplayName("Điều phối Request hợp lệ")
    class SuccessfulDispatch {

        @Test
        @DisplayName("Điều phối thành công trả về KetQua.ThanhCong mang đúng kết quả")
        void dispatchSuccessReturnsThanhCong() {
            registry.register("UPPER", String::toUpperCase);
            Request request = Request.of("UPPER", "hello world");

            KetQua result = dispatcher.dispatch(request);

            assertInstanceOf(KetQua.ThanhCong.class, result, "Kết quả phải là instance của KetQua.ThanhCong");
            KetQua.ThanhCong thanhCong = (KetQua.ThanhCong) result;
            assertEquals("HELLO WORLD", thanhCong.duLieu(), "Dữ liệu kết quả phải khớp với hàm xử lý");
        }

        @Test
        @DisplayName("Handler trả về chuỗi rỗng vẫn được coi là thành công")
        void dispatchEmptyStringResultReturnsThanhCong() {
            registry.register("EMPTY", arg -> "");
            Request request = Request.of("EMPTY", "");

            KetQua result = dispatcher.dispatch(request);

            assertInstanceOf(KetQua.ThanhCong.class, result);
            assertEquals("", ((KetQua.ThanhCong) result).duLieu());
        }

        @Test
        @DisplayName("Handler trả về null được chuẩn hóa thành KetQua.ThanhCong với chuỗi rỗng")
        void dispatchNullResultNormalizedToEmptyString() {
            registry.register("NULL_RET", arg -> null);
            Request request = Request.of("NULL_RET", "anything");

            KetQua result = dispatcher.dispatch(request);

            assertInstanceOf(KetQua.ThanhCong.class, result);
            assertEquals("", ((KetQua.ThanhCong) result).duLieu(),
                    "Handler trả về null phải được chuẩn hóa an toàn thành chuỗi rỗng, không làm sập hệ thống");
        }
    }

    @Nested
    @DisplayName("Xử lý lỗi: Lệnh không tồn tại")
    class UnknownCommandHandling {

        @Test
        @DisplayName("Lệnh chưa được đăng ký trong Registry trả về KetQua.ThatBai với mã ERR_UNKNOWN_COMMAND")
        void unregisteredCommandReturnsErrUnknownCommand() {
            Request request = Request.of("NOT_EXIST", "test");

            KetQua result = dispatcher.dispatch(request);

            assertInstanceOf(KetQua.ThatBai.class, result, "Kết quả phải là KetQua.ThatBai");
            KetQua.ThatBai thatBai = (KetQua.ThatBai) result;
            assertEquals(ErrorCode.ERR_UNKNOWN_COMMAND, thatBai.maLoi(), "Mã lỗi phải là ERR_UNKNOWN_COMMAND");
            assertNotNull(thatBai.thongDiep(), "Thông điệp lỗi không được null");
            assertFalse(thatBai.thongDiep().isBlank(), "Thông điệp lỗi không được rỗng");
        }
    }

    @Nested
    @DisplayName("Xử lý ngoại lệ nghiệp vụ CommandException")
    class CommandExceptionHandling {

        @Test
        @DisplayName("Handler ném CommandException có thông điệp riêng được chuyển thành KetQua.ThatBai chính xác")
        void handlerThrowsCommandExceptionWithCustomMessage() {
            registry.register("DIV", arg -> {
                throw new CommandException(ErrorCode.ERR_INVALID_ARGUMENT, "Không thể chia cho 0");
            });
            Request request = Request.of("DIV", "10 0");

            KetQua result = dispatcher.dispatch(request);

            assertInstanceOf(KetQua.ThatBai.class, result);
            KetQua.ThatBai thatBai = (KetQua.ThatBai) result;
            assertEquals(ErrorCode.ERR_INVALID_ARGUMENT, thatBai.maLoi());
            assertEquals("Không thể chia cho 0", thatBai.thongDiep());
        }

        @Test
        @DisplayName("Handler ném CommandException dùng thông điệp mặc định của ErrorCode")
        void handlerThrowsCommandExceptionWithDefaultMessage() {
            registry.register("SYNTAX_FAIL", arg -> {
                throw new CommandException(ErrorCode.ERR_SYNTAX, null);
            });
            Request request = Request.of("SYNTAX_FAIL", "");

            KetQua result = dispatcher.dispatch(request);

            assertInstanceOf(KetQua.ThatBai.class, result);
            KetQua.ThatBai thatBai = (KetQua.ThatBai) result;
            assertEquals(ErrorCode.ERR_SYNTAX, thatBai.maLoi());
            assertEquals(ErrorCode.ERR_SYNTAX.getDefaultMessage(), thatBai.thongDiep());
        }
    }

    @Nested
    @DisplayName("Xử lý ngoại lệ ngoài dự kiến (Unexpected RuntimeException)")
    class UnexpectedExceptionHandling {

        @Test
        @DisplayName("Handler ném RuntimeException ngoài dự kiến được bọc thành KetQua.ThatBai(ERR_INTERNAL)")
        void handlerThrowsUnexpectedRuntimeExceptionReturnsErrInternal() {
            registry.register("BUGGY", arg -> {
                throw new ArithmeticException("Divide by zero raw");
            });
            Request request = Request.of("BUGGY", "");

            KetQua result = dispatcher.dispatch(request);

            assertInstanceOf(KetQua.ThatBai.class, result, "Lỗi ngoài dự kiến phải được bọc thành ThatBai");
            KetQua.ThatBai thatBai = (KetQua.ThatBai) result;
            assertEquals(ErrorCode.ERR_INTERNAL, thatBai.maLoi(), "Mã lỗi phải là ERR_INTERNAL");
            assertNotNull(thatBai.thongDiep(), "Thông điệp lỗi phải tồn tại");
        }

        @Test
        @DisplayName("Handler ném NullPointerException được bắt an toàn không làm sập luồng điều phối")
        void handlerThrowsNullPointerExceptionHandledSafely() {
            registry.register("CRASH", arg -> {
                String str = null;
                return str.trim(); // gây NullPointerException
            });
            Request request = Request.of("CRASH", "");

            assertDoesNotThrow(() -> {
                KetQua result = dispatcher.dispatch(request);
                assertInstanceOf(KetQua.ThatBai.class, result);
                assertEquals(ErrorCode.ERR_INTERNAL, ((KetQua.ThatBai) result).maLoi());
            }, "Dispatch phải bảo vệ an toàn luồng, không để lọt ngoại lệ ra ngoài");
        }
    }

    @Nested
    @DisplayName("Kiểm tra tham số đầu vào của dispatch")
    class InputValidation {

        @Test
        @DisplayName("Truyền request null ném IllegalArgumentException")
        void nullRequestThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> dispatcher.dispatch(null),
                    "Request truyền vào dispatch không được null");
        }
    }
}

