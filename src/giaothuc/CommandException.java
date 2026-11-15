package giaothuc;

/**
 * Ngoại lệ nghiệp vụ dùng chung của hệ thống, kế thừa {@link RuntimeException}
 * để tương thích hoàn toàn với Functional Interface {@code Function<String, String>}
 * mà không gây lỗi biên dịch trong biểu thức Lambda.
 */
public class CommandException extends RuntimeException {

    private final ErrorCode errorCode;

    public CommandException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CommandException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
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

