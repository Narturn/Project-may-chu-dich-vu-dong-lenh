package giaothuc;

import java.time.LocalDateTime;

/**
 * Bản tin yêu cầu từ máy khách sau khi phân tích cú pháp thành công.
 * <p>
 * Lưu ý: Việc chuẩn hóa tên câu lệnh (chuyển thành chữ hoa, cắt tỉa khoảng trắng thừa)
 * thuộc trách nhiệm của {@code ProtocolParser} (F03) trước khi khởi tạo {@link Request}.
 *
 * @param command   tên câu lệnh
 * @param arguments chuỗi tham số đi kèm câu lệnh (nếu có)
 * @param timestamp thời điểm tiếp nhận yêu cầu
 */
public record Request(String command, String arguments, LocalDateTime timestamp) {

    /**
     * Compact constructor kiểm tra tính hợp lệ và chuẩn hóa dữ liệu bản tin yêu cầu.
     */
    public Request {
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("Tên lệnh không được null hoặc rỗng");
        }
        if (command.contains("\r") || command.contains("\n")) {
            throw new IllegalArgumentException("Tên lệnh không được chứa ký tự xuống dòng (\\r, \\n)");
        }
        if (arguments == null) {
            arguments = "";
        } else if (arguments.contains("\r") || arguments.contains("\n")) {
            throw new IllegalArgumentException("Tham số không được chứa ký tự xuống dòng (\\r, \\n)");
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    /**
     * Bí danh (alias) cho trường arguments.
     *
     * @return chuỗi tham số
     */
    public String args() {
        return arguments;
    }

    /**
     * Phương thức nhà máy tiện ích tạo Request với thời điểm hiện tại.
     * Nhận tên lệnh trực tiếp từ đối số được truyền vào, không tự chuẩn hóa.
     *
     * @param command   tên câu lệnh (nhận trực tiếp đối số truyền vào, không tự chuẩn hóa)
     * @param arguments chuỗi tham số
     * @return đối tượng {@link Request}
     */
    public static Request of(String command, String arguments) {
        return new Request(command, arguments, LocalDateTime.now());
    }
}

