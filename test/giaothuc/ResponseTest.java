package giaothuc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("F01, F10 - Kiểm thử mô hình Response, StatusCode và chuẩn hóa phản hồi")
class ResponseTest {

    @Nested
    @DisplayName("Kiểm thử StatusCode")
    class StatusCodeTests {

        @Test
        @DisplayName("StatusCode.OK và StatusCode.ERR có mã chuẩn tương ứng")
        void testStatusCodeValues() {
            assertEquals("OK", StatusCode.OK.code());
            assertEquals("ERR", StatusCode.ERR.code());
        }
    }

    @Nested
    @DisplayName("Khởi tạo Response hợp lệ")
    class ValidCreationTests {

        @Test
        @DisplayName("Tạo Response.ok với dữ liệu hợp lệ")
        void testResponseOkWithData() {
            Response response = Response.ok("Xin chao");

            assertEquals(StatusCode.OK, response.status());
            assertEquals("Xin chao", response.payload());
            assertEquals("OK Xin chao\n", response.toProtocolString());
        }

        @Test
        @DisplayName("Tạo Response.ok với dữ liệu rỗng hoặc null")
        void testResponseOkWithEmptyOrNull() {
            Response respEmpty = Response.ok("");
            assertEquals(StatusCode.OK, respEmpty.status());
            assertEquals("", respEmpty.payload());
            assertEquals("OK\n", respEmpty.toProtocolString());

            Response respNull = Response.ok(null);
            assertEquals(StatusCode.OK, respNull.status());
            assertEquals("", respNull.payload());
            assertEquals("OK\n", respNull.toProtocolString());
        }

        @Test
        @DisplayName("Tạo Response.err với ErrorCode và thông điệp")
        void testResponseErrWithCodeAndMessage() {
            Response response = Response.err(ErrorCode.ERR_SYNTAX, "Thieu tham so");

            assertEquals(StatusCode.ERR, response.status());
            assertEquals("ERR_SYNTAX Thieu tham so", response.payload());
            assertEquals("ERR ERR_SYNTAX Thieu tham so\n", response.toProtocolString());
        }

        @Test
        @DisplayName("Tạo Response.err với thông điệp null hoặc rỗng")
        void testResponseErrWithEmptyMessage() {
            Response respNullMsg = Response.err(ErrorCode.ERR_UNKNOWN_COMMAND, null);
            assertEquals("ERR_UNKNOWN_COMMAND", respNullMsg.payload());
            assertEquals("ERR ERR_UNKNOWN_COMMAND\n", respNullMsg.toProtocolString());

            Response respBlankMsg = Response.err(ErrorCode.ERR_UNKNOWN_COMMAND, "   ");
            assertEquals("ERR_UNKNOWN_COMMAND", respBlankMsg.payload());
            assertEquals("ERR ERR_UNKNOWN_COMMAND\n", respBlankMsg.toProtocolString());
        }

        @Test
        @DisplayName("Tạo Response.err khi ErrorCode là null thì fallback về ERR_INTERNAL")
        void testResponseErrWithNullCode() {
            Response response = Response.err((ErrorCode) null, "Loi khong xac dinh");

            assertEquals(StatusCode.ERR, response.status());
            assertEquals("ERR_INTERNAL Loi khong xac dinh", response.payload());
            assertEquals("ERR ERR_INTERNAL Loi khong xac dinh\n", response.toProtocolString());
        }

        @Test
        @DisplayName("F10: Tạo Response.err với mã lỗi chuỗi hợp lệ")
        void testResponseErrWithStringCodeValid() {
            Response response = Response.err("ERR_SYNTAX", "Sai cu phap");

            assertEquals(StatusCode.ERR, response.status());
            assertEquals("ERR_SYNTAX Sai cu phap", response.payload());
            assertEquals("ERR ERR_SYNTAX Sai cu phap\n", response.toProtocolString());
        }

        @Test
        @DisplayName("F10: Mã lỗi chuỗi không xác định hoặc không hợp lệ fallback an toàn về ERR_INTERNAL")
        void testResponseErrWithStringCodeInvalidOrUnknown() {
            Response respUnknown = Response.err("ERR_NON_EXISTENT", "Loi la");
            assertEquals("ERR_INTERNAL Loi la", respUnknown.payload());
            assertEquals("ERR ERR_INTERNAL Loi la\n", respUnknown.toProtocolString());

            Response respBlank = Response.err("   ", "Loi rong");
            assertEquals("ERR_INTERNAL Loi rong", respBlank.payload());
            assertEquals("ERR ERR_INTERNAL Loi rong\n", respBlank.toProtocolString());

            Response respNull = Response.err((String) null, "Loi null");
            assertEquals("ERR_INTERNAL Loi null", respNull.payload());
            assertEquals("ERR ERR_INTERNAL Loi null\n", respNull.toProtocolString());
        }

        @Test
        @DisplayName("F10: Thông báo lỗi có khoảng trắng thừa ở đầu/cuối được chuẩn hóa trim gọn gàng")
        void testErrorMessageTrimming() {
            Response response = Response.err(ErrorCode.ERR_INVALID_ARGUMENT, "   Tham so am   ");
            assertEquals("ERR_INVALID_ARGUMENT Tham so am", response.payload());
            assertEquals("ERR ERR_INVALID_ARGUMENT Tham so am\n", response.toProtocolString());
        }
    }

    @Nested
    @DisplayName("Các trường hợp biên: null, ký tự xuống dòng nhiều tầng")
    class EdgeAndSanitizationCasesTests {

        @Test
        @DisplayName("Status là null phải ném ngoại lệ")
        void testNullStatusThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> new Response(null, "data"));
        }

        @Test
        @DisplayName("Payload null trong canonical constructor tự động chuyển thành chuỗi rỗng")
        void testNullPayloadDefaultsToEmpty() {
            Response response = new Response(StatusCode.OK, null);
            assertEquals("", response.payload());
            assertEquals("OK\n", response.toProtocolString());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "dong1\ndong2",
                "dong1\r\ndong2",
                "dong1\rdong2",
                "dong1\n\ndong2\r\ndong3\n"
        })
        @DisplayName("Dữ liệu phản hồi chứa ký tự xuống dòng nhiều tầng phải được làm phẳng/escape để không vỡ dòng giao thức TCP")
        void testMultiLinePayloadSanitization(String multiLinePayload) {
            Response response = new Response(StatusCode.OK, multiLinePayload);

            // Kiểm tra payload không còn chứa ký tự ngắt dòng vật lý '\r' hoặc '\n'
            assertFalse(response.payload().contains("\r"), "Payload không được chứa ký tự \\r trần");
            assertFalse(response.payload().contains("\n"), "Payload không được chứa ký tự \\n trần");

            // Chuỗi giao thức toProtocolString() phải có đúng 1 ký tự xuống dòng ở cuối
            // cùng
            String protoStr = response.toProtocolString();
            assertTrue(protoStr.endsWith("\n"));
            assertEquals(1, protoStr.chars().filter(ch -> ch == '\n').count(),
                    "Bản tin giao thức gửi qua TCP phải đúng 1 dòng (chỉ chứa 1 ký tự \\n ở cuối)");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Loi dong 1\r\nLoi dong 2",
                "Loi dong 1\nLoi dong 2",
                "Loi dong 1\rLoi dong 2",
                "Loi dong 1\n\nLoi dong 2\r\nLoi dong 3\n"
        })
        @DisplayName("F10: Thông báo lỗi chứa ký tự xuống dòng phải được làm phẳng thành 1 dòng giao thức")
        void testErrorMessageSanitization(String multiLineError) {
            Response response = Response.err(ErrorCode.ERR_SYNTAX, multiLineError);

            assertFalse(response.payload().contains("\r"), "Payload lỗi không được chứa ký tự \\r trần");
            assertFalse(response.payload().contains("\n"), "Payload lỗi không được chứa ký tự \\n trần");

            String protoStr = response.toProtocolString();
            assertTrue(protoStr.startsWith("ERR ERR_SYNTAX "), "Bản tin phải bắt đầu bằng ERR ERR_SYNTAX");
            assertTrue(protoStr.endsWith("\n"), "Bản tin phải kết thúc bằng \\n");
            assertTrue(response.payload().contains("\\n"), "Ký tự xuống dòng phải được chuyển thành chuỗi literal \\n");
            assertEquals(1, protoStr.chars().filter(ch -> ch == '\n').count(),
                    "Bản tin phản hồi lỗi TCP phải đúng 1 dòng duy nhất");
        }
    }

    @Nested
    @DisplayName("Tính bất biến và tính nhất quán Record")
    class ImmutabilityTests {

        @Test
        @DisplayName("Hai Response có cùng trạng thái và payload phải bằng nhau")
        void testRecordEqualsAndHashCode() {
            Response r1 = Response.ok("du lieu");
            Response r2 = new Response(StatusCode.OK, "du lieu");

            assertEquals(r1, r2);
            assertEquals(r1.hashCode(), r2.hashCode());
        }
    }
}
