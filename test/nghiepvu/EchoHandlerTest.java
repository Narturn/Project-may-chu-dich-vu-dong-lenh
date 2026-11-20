package nghiepvu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("F07 - Kiểm thử EchoHandler")
class EchoHandlerTest {

    private EchoHandler echoHandler;

    @BeforeEach
    void setUp() {
        echoHandler = new EchoHandler();
    }

    @Test
    @DisplayName("Phản hồi nguyên văn chuỗi ASCII thông thường")
    void echoesNormalString() {
        assertEquals("Hello World 123", echoHandler.apply("Hello World 123"));
    }

    @Test
    @DisplayName("Bảo toàn nguyên văn chuỗi tiếng Việt có dấu Unicode")
    void echoesVietnameseUnicodeString() {
        String input = "Xin chào Việt Nam: đ, Đ, ê, ư, ơ, ă, â, Ế, Ứ, Ở, Ẵ, Ậ";
        assertEquals(input, echoHandler.apply(input));
    }

    @Test
    @DisplayName("Trả về chuỗi rỗng khi tham số là chuỗi rỗng hoặc null")
    void returnsEmptyStringForEmptyOrNullInput() {
        assertEquals("", echoHandler.apply(""));
        assertEquals("", echoHandler.apply(null));
    }

    @Test
    @DisplayName("Không cắt bỏ khoảng trắng ở đầu và cuối chuỗi tham số")
    void preservesLeadingAndTrailingWhitespace() {
        String input = "   khoảng trắng đầu và cuối   ";
        assertEquals(input, echoHandler.apply(input));
        assertEquals("   ", echoHandler.apply("   "));
    }

    @Test
    @DisplayName("Bảo toàn ký tự tab và các ký tự đặc biệt")
    void preservesTabsAndSpecialCharacters() {
        String input = "\t cột 1 \t\t cột 2 !@#$%^&*()_+-=[]{}|;':\",./<>?~`";
        assertEquals(input, echoHandler.apply(input));
    }
}

