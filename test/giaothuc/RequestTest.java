package giaothuc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("F01 - Kiểm thử mô hình Request")
class RequestTest {

    @Nested
    @DisplayName("Khởi tạo hợp lệ")
    class ValidCreationTests {

        @Test
        @DisplayName("Khởi tạo Request đầy đủ tham số")
        void testCanonicalConstructorWithValidData() {
            LocalDateTime now = LocalDateTime.now();
            Request request = new Request("ECHO", "hello world", now);

            assertEquals("ECHO", request.command());
            assertEquals("hello world", request.arguments());
            assertEquals("hello world", request.args());
            assertEquals(now, request.timestamp());
        }

        @Test
        @DisplayName("Phương thức of() gán timestamp tự động")
        void testFactoryMethodOf() {
            Request request = Request.of("TIME", "");

            assertEquals("TIME", request.command());
            assertEquals("", request.arguments());
            assertEquals("", request.args());
            assertNotNull(request.timestamp());
        }

        @Test
        @DisplayName("Tham số null tự động chuẩn hóa thành chuỗi rỗng")
        void testNullArgumentsDefaultsToEmpty() {
            Request request = Request.of("DATE", null);

            assertEquals("DATE", request.command());
            assertEquals("", request.arguments());
            assertEquals("", request.args());
        }

        @Test
        @DisplayName("Timestamp null tự động gán thời điểm hiện tại")
        void testNullTimestampDefaultsToNow() {
            Request request = new Request("QUIT", "", null);

            assertEquals("QUIT", request.command());
            assertNotNull(request.timestamp());
        }
    }

    @Nested
    @DisplayName("Các trường hợp biên và dữ liệu không hợp lệ")
    class EdgeAndInvalidCasesTests {

        @Test
        @DisplayName("Tên lệnh là null phải ném ngoại lệ")
        void testNullCommandThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> new Request(null, "arg", LocalDateTime.now()));
            assertThrows(IllegalArgumentException.class, () -> Request.of(null, "arg"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   ", "\t"})
        @DisplayName("Tên lệnh rỗng hoặc chỉ có khoảng trắng phải ném ngoại lệ")
        void testBlankCommandThrowsException(String blankCommand) {
            assertThrows(IllegalArgumentException.class, () -> new Request(blankCommand, "arg", LocalDateTime.now()));
            assertThrows(IllegalArgumentException.class, () -> Request.of(blankCommand, "arg"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"ECHO\n", "ECHO\r", "EC\r\nHO"})
        @DisplayName("Tên lệnh chứa ký tự điều khiển xuống dòng phải ném ngoại lệ")
        void testCommandWithControlCharactersThrowsException(String invalidCommand) {
            assertThrows(IllegalArgumentException.class, () -> Request.of(invalidCommand, "hello"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"hello\nworld", "hello\rworld", "hello\r\nworld", "line1\nline2\nline3"})
        @DisplayName("Tham số chứa ký tự xuống dòng gây vỡ giao thức phải ném ngoại lệ")
        void testArgumentsWithNewlineThrowsException(String invalidArgs) {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> Request.of("ECHO", invalidArgs)
            );
            assertTrue(ex.getMessage().contains("ký tự xuống dòng") || ex.getMessage().contains("newline"));
        }
    }

    @Nested
    @DisplayName("Tính bất biến và tính nhất quán của Record")
    class ImmutabilityAndRecordSemanticsTests {

        @Test
        @DisplayName("Hai đối tượng Request cùng dữ liệu phải bằng nhau (equals và hashCode)")
        void testRecordEqualsAndHashCode() {
            LocalDateTime time = LocalDateTime.of(2026, 11, 19, 10, 0, 0);
            Request req1 = new Request("UPPER", "test", time);
            Request req2 = new Request("UPPER", "test", time);

            assertEquals(req1, req2);
            assertEquals(req1.hashCode(), req2.hashCode());
        }

        @Test
        @DisplayName("toString() phải chứa đầy đủ thông tin")
        void testToStringContainsEssentialFields() {
            Request request = Request.of("STATS", "");
            String str = request.toString();

            assertTrue(str.contains("STATS"));
            assertTrue(str.contains("arguments="));
            assertTrue(str.contains("timestamp="));
        }
    }
}

