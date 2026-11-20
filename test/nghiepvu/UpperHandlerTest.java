package nghiepvu;

import giaothuc.CommandException;
import giaothuc.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("F07 - Kiểm thử UpperHandler")
class UpperHandlerTest {

    private UpperHandler upperHandler;

    @BeforeEach
    void setUp() {
        upperHandler = new UpperHandler();
    }

    @Test
    @DisplayName("Chuyển chuỗi ASCII thông thường thành chữ in hoa")
    void convertsAsciiStringToUpperCase() {
        assertEquals("HELLO WORLD 123!", upperHandler.apply("hello World 123!"));
    }

    @Test
    @DisplayName("Chuyển chuỗi tiếng Việt có dấu Unicode thành chữ in hoa và không phụ thuộc locale máy")
    void convertsVietnameseUnicodeToUpperCase() {
        assertEquals("XIN CHÀO VIỆT NAM: Đ, Ê, Ư, Ơ, Ă, Â",
                upperHandler.apply("xin chào việt nam: đ, ê, ư, ơ, ă, â"));
        assertEquals("TITLE", upperHandler.apply("title"));
    }

    @Test
    @DisplayName("Giữ nguyên khoảng trắng đầu/cuối khi chuyển sang chữ in hoa (không trim)")
    void doesNotTrimContentBeforeConverting() {
        assertEquals("  HELLO\tWORLD  ", upperHandler.apply("  hello\tworld  "));
    }

    @Test
    @DisplayName("Ném CommandException với ERR_INVALID_ARGUMENT khi tham số là null")
    void throwsCommandExceptionWhenInputIsNull() {
        CommandException ex = assertThrows(CommandException.class, () -> upperHandler.apply(null));
        assertEquals(ErrorCode.ERR_INVALID_ARGUMENT, ex.getErrorCode());
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }

    @Test
    @DisplayName("Ném CommandException với ERR_INVALID_ARGUMENT khi tham số là chuỗi rỗng")
    void throwsCommandExceptionWhenInputIsEmpty() {
        CommandException ex = assertThrows(CommandException.class, () -> upperHandler.apply(""));
        assertEquals(ErrorCode.ERR_INVALID_ARGUMENT, ex.getErrorCode());
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "\t", " \t  "})
    @DisplayName("Ném CommandException với ERR_INVALID_ARGUMENT khi tham số chỉ chứa khoảng trắng")
    void throwsCommandExceptionWhenInputIsWhitespaceOnly(String blankInput) {
        CommandException ex = assertThrows(CommandException.class, () -> upperHandler.apply(blankInput));
        assertEquals(ErrorCode.ERR_INVALID_ARGUMENT, ex.getErrorCode());
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }
}

