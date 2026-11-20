package giaothuc;

/**
 * Ngoại lệ nghiệp vụ dùng chung của hệ thống, kế thừa {@link RuntimeException}
 * để tương thích hoàn toàn với Functional Interface {@code Function<String, String>}
 * mà không gây lỗi biên dịch trong biểu thức Lambda.
 */
public class CommandException extends RuntimeException {

    private final ErrorCode errorCode;

    private static String normalizeMessage(ErrorCode errorCode, String message) {
        if (errorCode == null) {
            throw new IllegalArgumentException("Mã lỗi không được null");
        }
        return message == null || message.isBlank() ? errorCode.getDefaultMessage() : message;
    }

    /**
     * Tạo lỗi F02 với mã bắt buộc và thông điệp; nếu thông điệp trống thì dùng mô tả mặc định.
     *
     * @throws IllegalArgumentException nếu mã lỗi là null
     */
    public CommandException(ErrorCode errorCode, String message) {
        super(normalizeMessage(errorCode, message));
        this.errorCode = errorCode;
    }

    /**
     * Tạo lỗi F02 và giữ nguyên nguyên nhân gốc để truy vết khi xử lý hoặc ghi log.
     *
     * @throws IllegalArgumentException nếu mã lỗi là null
     */
    public CommandException(ErrorCode errorCode, String message, Throwable cause) {
        super(normalizeMessage(errorCode, message), cause);
        this.errorCode = errorCode;
    }

    /**
     * Lấy mã lỗi tương ứng của ngoại lệ.
     *
     * @return mã lỗi enum {@link ErrorCode}
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

