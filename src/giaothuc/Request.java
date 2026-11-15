package giaothuc;

import java.time.LocalDateTime;

/**
 * Bản tin yêu cầu từ máy khách sau khi phân tích cú pháp thành công.
 *
 * @param command   tên câu lệnh đã chuẩn hóa (chữ hoa)
 * @param arguments chuỗi tham số đi kèm câu lệnh (nếu có)
 * @param timestamp thời điểm tiếp nhận yêu cầu
 */
public record Request(String command, String arguments, LocalDateTime timestamp) {

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
     *
     * @param command   tên câu lệnh
     * @param arguments chuỗi tham số
     * @return đối tượng {@link Request}
     */
    public static Request of(String command, String arguments) {
        return new Request(command, arguments, LocalDateTime.now());
    }
}

