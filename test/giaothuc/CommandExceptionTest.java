package giaothuc;

import org.junit.jupiter.api.Test;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class CommandExceptionTest {
    @Test
    void preservesErrorCodeAndCustomMessage() {
        CommandException error = new CommandException(ErrorCode.ERR_SYNTAX, "Thiếu tên lệnh");
        assertSame(ErrorCode.ERR_SYNTAX, error.getErrorCode());
        assertEquals("Thiếu tên lệnh", error.getMessage());
        assertNull(error.getCause());
    }

    @Test
    void preservesOriginalCauseAndStackTrace() {
        IllegalArgumentException cause = new IllegalArgumentException("Dữ liệu không hợp lệ");
        CommandException error = new CommandException(ErrorCode.ERR_INVALID_ARGUMENT, "Sai tham số", cause);
        assertSame(cause, error.getCause());
        assertEquals("Sai tham số", error.getMessage());
        assertSame(ErrorCode.ERR_INVALID_ARGUMENT, error.getErrorCode());
        assertTrue(error.getStackTrace().length > 0);
    }

    @Test
    void usesDefaultMessageWhenMessageIsMissing() {
        for (ErrorCode code : ErrorCode.values()) {
            for (String message : new String[] {null, "", " \t\n"}) {
                assertEquals(code.getDefaultMessage(), new CommandException(code, message).getMessage());
            }
        }
    }

    @Test
    void usesDefaultMessageWithoutLosingCause() {
        RuntimeException cause = new RuntimeException("Lỗi gốc");
        CommandException error = new CommandException(ErrorCode.ERR_INTERNAL, null, cause);
        assertEquals(ErrorCode.ERR_INTERNAL.getDefaultMessage(), error.getMessage());
        assertSame(cause, error.getCause());
    }

    @Test
    void rejectsMissingErrorCodeInBothConstructors() {
        assertThrows(IllegalArgumentException.class, () -> new CommandException(null, "Lỗi"));
        assertThrows(IllegalArgumentException.class,
                () -> new CommandException(null, "Lỗi", new RuntimeException()));
        assertThrows(IllegalArgumentException.class, () -> new CommandException(null, null));
    }

    @Test
    void canBeThrownByFunctionLambdaWithoutCheckedException() {
        Function<String, String> handler = args -> {
            throw new CommandException(ErrorCode.ERR_INVALID_ARGUMENT, "Sai tham số");
        };
        CommandException error = assertThrows(CommandException.class, () -> handler.apply("abc"));
        assertInstanceOf(RuntimeException.class, error);
        assertSame(ErrorCode.ERR_INVALID_ARGUMENT, error.getErrorCode());
    }
}
