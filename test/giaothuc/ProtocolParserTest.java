package giaothuc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolParserTest {
    private final ProtocolParser parser = new ProtocolParser();

    @ParameterizedTest
    @ValueSource(strings = {"time", "Time", "TIME", "tImE"})
    void normalizesCommandCase(String input) {
        Request request = parser.parse(input);
        assertEquals("TIME", request.command());
        assertEquals("", request.arguments());
        assertNotNull(request.timestamp());
    }

    @ParameterizedTest
    @ValueSource(strings = {"  echo   xin chào  ", "\techo\t \txin chào\t"})
    void trimsOuterWhitespaceAndSkipsArgumentSeparator(String input) {
        Request request = parser.parse(input);
        assertEquals("ECHO", request.command());
        assertEquals("xin chào", request.arguments());
    }

    @Test
    void preservesArgumentCaseUnicodeAndInternalWhitespace() {
        String arguments = "Xin  chào\tViệt Nam! \\\"nội dung\\\"";
        Request request = parser.parse("upper " + arguments);
        assertEquals("UPPER", request.command());
        assertEquals(arguments, request.arguments());
    }

    @Test
    void acceptsCommandWithOnlyTrailingWhitespace() {
        assertEquals("", parser.parse("DATE \t ").arguments());
    }

    @Test
    void leavesUnknownCommandsForDispatcher() {
        Request request = parser.parse("custom-command abc");
        assertEquals("CUSTOM-COMMAND", request.command());
        assertEquals("abc", request.arguments());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", " \t ", "\n", "\r\n"})
    void rejectsMissingOrBlankLine(String input) {
        assertSyntaxError(input);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ECHO hello\n", "ECHO hello\r\n", "ECHO hello\r"})
    void acceptsOneTrailingLineTerminator(String input) {
        assertEquals("hello", parser.parse(input).arguments());
    }

    @ParameterizedTest
    @ValueSource(strings = {"EC\nHO hello", "ECHO hello\rworld", "TIME\nDATE",
            "TIME\r\nDATE", "TIME\n\n", "\nTIME"})
    void rejectsEmbeddedOrMultipleLineTerminators(String input) {
        assertSyntaxError(input);
    }

    @Test
    void acceptsExactlyMaximumLineLengthIncludingWithCrLf() {
        String arguments = "x".repeat(ProtocolParser.MAX_LINE_LENGTH - "ECHO ".length());
        String line = "ECHO " + arguments;
        assertEquals(arguments, parser.parse(line).arguments());
        assertEquals(arguments, parser.parse(line + "\r\n").arguments());
    }

    @Test
    void rejectsLineOverMaximumLength() {
        assertSyntaxError("ECHO " + "x".repeat(ProtocolParser.MAX_LINE_LENGTH - 4));
    }

    @Test
    void checksLengthBeforeTrimmingWhitespace() {
        assertSyntaxError(" ".repeat(ProtocolParser.MAX_LINE_LENGTH) + "TIME");
    }

    @Test
    void normalizationDoesNotDependOnSystemLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("TIME", parser.parse("time").command());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void parsesConsecutiveLinesReadWithLfAndCrLf() throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader("time\r\necho xin chào\n"))) {
            assertEquals("TIME", parser.parse(reader.readLine()).command());
            assertEquals("xin chào", parser.parse(reader.readLine()).arguments());
            assertNull(reader.readLine());
        }
    }

    private void assertSyntaxError(String input) {
        CommandException error = assertThrows(CommandException.class, () -> parser.parse(input));
        assertSame(ErrorCode.ERR_SYNTAX, error.getErrorCode());
        assertNotNull(error.getMessage());
        assertFalse(error.getMessage().isBlank());
    }
}
