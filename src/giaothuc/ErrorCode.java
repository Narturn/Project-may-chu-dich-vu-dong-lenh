package giaothuc;

/**
 * Danh mục mã lỗi giao thức chuẩn của hệ thống máy chủ dòng lệnh.
 */
public enum ErrorCode {
    ERR_UNKNOWN_COMMAND("Lệnh không tồn tại hoặc chưa được hỗ trợ"),
    ERR_SYNTAX("Cú pháp câu lệnh không hợp lệ"),
    ERR_INVALID_ARGUMENT("Tham số câu lệnh không hợp lệ"),
    ERR_INTERNAL("Lỗi xử lý nội bộ của máy chủ"),
    ERR_RATE_LIMIT_EXCEEDED("Vượt quá giới hạn tần suất yêu cầu");

    private final String defaultMessage;

    ErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    /**
     * Lấy thông điệp mô tả lỗi mặc định.
     *
     * @return mô tả lỗi
     */
    public String getDefaultMessage() {
        return defaultMessage;
    }
}

