package nghiepvu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("F04 - Kiểm thử CommandRegistry")
class CommandRegistryTest {

    private CommandRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CommandRegistry();
    }

    @Nested
    @DisplayName("Đăng ký và tra cứu Handler thành công")
    class SuccessfulRegistrationAndLookup {

        @Test
        @DisplayName("Đăng ký handler bằng biểu thức Lambda thành công")
        void registerWithLambda() {
            Function<String, String> echoHandler = arg -> arg;
            registry.register("ECHO", echoHandler);

            Optional<Function<String, String>> handlerOpt = registry.getHandler("ECHO");
            assertTrue(handlerOpt.isPresent(), "Handler ECHO phải tìm thấy trong registry");
            assertEquals("hello", handlerOpt.get().apply("hello"));
        }

        @Test
        @DisplayName("Đăng ký handler bằng Method Reference thành công")
        void registerWithMethodReference() {
            registry.register("UPPER", String::toUpperCase);

            Optional<Function<String, String>> handlerOpt = registry.getHandler("UPPER");
            assertTrue(handlerOpt.isPresent(), "Handler UPPER phải tìm thấy trong registry");
            assertEquals("TEST", handlerOpt.get().apply("test"));
        }

        @Test
        @DisplayName("Đăng ký chữ thường và tra cứu chữ hoa vẫn tìm thấy cùng handler")
        void registerLowerCaseLookupUpperCase() {
            registry.register("echo", arg -> "echo:" + arg);

            Optional<Function<String, String>> upperOpt = registry.getHandler("ECHO");
            assertTrue(upperOpt.isPresent(), "Tra cứu 'ECHO' phải tìm thấy lệnh đăng ký bằng 'echo'");
            assertEquals("echo:hi", upperOpt.get().apply("hi"));

            Optional<Function<String, String>> mixedOpt = registry.getHandler("  eChO  ");
            assertTrue(mixedOpt.isPresent(), "Tra cứu '  eChO  ' phải tìm thấy cùng handler");
            assertEquals("echo:hi", mixedOpt.get().apply("hi"));
        }

        @Test
        @DisplayName("Lấy danh sách các lệnh được hỗ trợ (getSupportedCommands) là bản chụp bất biến (snapshot)")
        void getSupportedCommandsReturnsImmutableSnapshot() {
            registry.register("TIME", arg -> "10:00:00");
            registry.register("DATE", arg -> "2026-11-21");

            Set<String> snapshot = registry.getSupportedCommands();
            assertEquals(2, snapshot.size());
            assertTrue(snapshot.contains("TIME"));
            assertTrue(snapshot.contains("DATE"));

            // 1. Kiểm tra tính bất biến (không thể sửa đổi từ bên ngoài)
            assertThrows(UnsupportedOperationException.class, () -> snapshot.add("HACK"),
                    "Tập hợp lệnh trả về không được phép thêm phần tử trực tiếp từ bên ngoài");

            // 2. Kiểm tra tính snapshot: đăng ký thêm lệnh mới sau khi lấy snapshot
            // snapshot cũ không được bị thay đổi kích thước
            registry.register("CALC", arg -> "result");
            assertEquals(2, snapshot.size(),
                    "Snapshot cũ không được bị thay đổi khi registry đăng ký thêm lệnh sau đó");
            assertFalse(snapshot.contains("CALC"),
                    "Snapshot cũ không chứa lệnh CALC được đăng ký sau");

            // Lần gọi mới trả về snapshot mới chứa đủ 3 lệnh
            Set<String> updatedSnapshot = registry.getSupportedCommands();
            assertEquals(3, updatedSnapshot.size());
            assertTrue(updatedSnapshot.contains("CALC"));
        }
    }

    @Nested
    @DisplayName("Tra cứu lệnh không tồn tại")
    class NonExistentCommandLookup {

        @Test
        @DisplayName("Tra cứu lệnh chưa đăng ký trả về Optional.empty()")
        void lookupUnregisteredCommandReturnsEmpty() {
            Optional<Function<String, String>> handlerOpt = registry.getHandler("UNKNOWN");
            assertTrue(handlerOpt.isEmpty(), "Lệnh chưa đăng ký phải trả về Optional.empty()");
        }

        @Test
        @DisplayName("Tra cứu lệnh với tên null hoặc rỗng trả về Optional.empty()")
        void lookupNullOrBlankCommandReturnsEmpty() {
            assertTrue(registry.getHandler(null).isEmpty(), "Tra cứu với tên null phải trả về empty");
            assertTrue(registry.getHandler("").isEmpty(), "Tra cứu với tên rỗng phải trả về empty");
            assertTrue(registry.getHandler("   ").isEmpty(), "Tra cứu với tên toàn khoảng trắng phải trả về empty");
        }
    }

    @Nested
    @DisplayName("Kiểm tra tính hợp lệ của tham số đăng ký")
    class RegistrationValidation {

        @Test
        @DisplayName("Đăng ký với handler null ném IllegalArgumentException")
        void registerNullHandlerThrowsException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> registry.register("TEST", null));
            assertTrue(ex.getMessage().contains("Handler không được null"));
        }

        @ParameterizedTest(name = "Tên lệnh không hợp lệ: \"{0}\"")
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Đăng ký với tên lệnh rỗng hoặc chỉ có khoảng trắng ném IllegalArgumentException")
        void registerBlankCommandThrowsException(String blankCmd) {
            assertThrows(IllegalArgumentException.class,
                    () -> registry.register(blankCmd, arg -> arg));
        }

        @Test
        @DisplayName("Đăng ký với tên lệnh null ném IllegalArgumentException")
        void registerNullCommandThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> registry.register(null, arg -> arg));
        }
    }

    @Nested
    @DisplayName("Xử lý trùng lặp tên lệnh (Quyết định từ chối đăng ký trùng)")
    class DuplicateRegistrationHandling {

        @Test
        @DisplayName("Đăng ký trùng tên lệnh đã tồn tại phải ném IllegalStateException")
        void registerDuplicateCommandThrowsIllegalStateException() {
            registry.register("CALC", arg -> "42");

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> registry.register("CALC", arg -> "100"),
                    "Hệ thống phải từ chối đăng ký trùng tên lệnh để tránh ghi đè ngoài ý muốn");
            assertTrue(ex.getMessage().contains("CALC"));
        }

        @Test
        @DisplayName("Sau khi ném ngoại lệ trùng lặp, handler ban đầu vẫn được giữ nguyên")
        void originalHandlerPreservedAfterDuplicateAttempt() {
            registry.register("ECHO", arg -> "original:" + arg);

            assertThrows(IllegalStateException.class,
                    () -> registry.register("ECHO", arg -> "new:" + arg));

            Optional<Function<String, String>> handlerOpt = registry.getHandler("ECHO");
            assertTrue(handlerOpt.isPresent());
            assertEquals("original:data", handlerOpt.get().apply("data"),
                    "Handler gốc phải được bảo toàn nguyên vẹn");
        }
    }
}

