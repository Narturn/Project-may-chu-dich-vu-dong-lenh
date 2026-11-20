package giaothuc;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ErrorCodeTest {
    @Test
    void containsExactlyTheAgreedProtocolErrorCodes() {
        Set<String> names = new HashSet<>();
        for (ErrorCode code : ErrorCode.values()) {
            names.add(code.name());
        }
        assertEquals(Set.of("ERR_UNKNOWN_COMMAND", "ERR_SYNTAX", "ERR_INVALID_ARGUMENT",
                "ERR_INTERNAL", "ERR_RATE_LIMIT_EXCEEDED"), names);
    }

    @Test
    void everyErrorHasANonBlankDefaultMessage() {
        for (ErrorCode code : ErrorCode.values()) {
            assertNotNull(code.getDefaultMessage(), code.name());
            assertFalse(code.getDefaultMessage().isBlank(), code.name());
        }
    }

    @Test
    void statusCodesRemainSeparateFromErrorCodes() {
        assertArrayEquals(new StatusCode[] {StatusCode.OK, StatusCode.ERR}, StatusCode.values());
        assertEquals("OK", StatusCode.OK.code());
        assertEquals("ERR", StatusCode.ERR.code());
    }
}
