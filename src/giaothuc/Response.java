package giaothuc;

/**
 * Bản tin phản hồi của giao thức mạng TCP được gửi trả về máy khách.
 *
 * @param status  mã trạng thái {@link StatusCode#OK} hoặc {@link StatusCode#ERR}
 * @param payload nội dung kết quả thành công hoặc mã lỗi kèm thông điệp chi tiết
 */
public record Response(StatusCode status, String payload) {

    /**
     * Compact constructor kiểm tra tính hợp lệ và chuẩn hóa dữ liệu bản tin phản hồi.
     */
    public Response {
        if (status == null) {
            throw new IllegalArgumentException("Mã trạng thái phản hồi không được null");
        }
        if (payload == null) {
            payload = "";
        } else {
            // Chuẩn hóa / làm phẳng ký tự xuống dòng nhiều tầng thành escape sequence "\n"
            // để bảo vệ giao thức văn bản 1 dòng qua TCP, tránh gây vỡ khung bản tin phía nhận.
            payload = payload.replace("\r\n", "\\n")
                             .replace("\r", "\\n")
                             .replace("\n", "\\n");
        }
    }

    /**
     * Tạo bản tin phản hồi thành công.
     *
     * @param data dữ liệu kết quả trả về
     * @return đối tượng {@link Response} với trạng thái OK
     */
    public static Response ok(String data) {
        return new Response(StatusCode.OK, data == null ? "" : data);
    }

    /**
     * Tạo bản tin phản hồi báo lỗi kèm mã lỗi chuẩn.
     *
     * @param code mã lỗi {@link ErrorCode}
     * @param msg  thông điệp lỗi chi tiết
     * @return đối tượng {@link Response} với trạng thái ERR
     */
    public static Response err(ErrorCode code, String msg) {
        String detail = (code != null ? code.name() : ErrorCode.ERR_INTERNAL.name())
                + (msg != null && !msg.isBlank() ? " " + msg.trim() : "");
        return new Response(StatusCode.ERR, detail);
    }

    /**
     * Tạo bản tin phản hồi báo lỗi từ mã lỗi dạng chuỗi và thông điệp chi tiết (F10).
     * Tự động chuyển đổi an toàn: nếu mã lỗi rỗng, null hoặc không nằm trong danh mục định nghĩa
     * thì fallback về {@link ErrorCode#ERR_INTERNAL} mà không làm chương trình bị crash.
     *
     * @param rawCode chuỗi mã lỗi (ví dụ: "ERR_SYNTAX")
     * @param msg     thông điệp lỗi chi tiết
     * @return đối tượng {@link Response} với trạng thái ERR
     */
    public static Response err(String rawCode, String msg) {
        ErrorCode code = ErrorCode.ERR_INTERNAL;
        if (rawCode != null && !rawCode.isBlank()) {
            try {
                code = ErrorCode.valueOf(rawCode.trim());
            } catch (IllegalArgumentException ignored) {
                // Mã lỗi không nằm trong danh mục đã định nghĩa -> fallback về ERR_INTERNAL
            }
        }
        return err(code, msg);
    }

    /**
     * Định dạng bản tin thành chuỗi văn bản một dòng gửi qua luồng Socket TCP.
     *
     * @return chuỗi giao thức một dòng kết thúc bằng ký tự xuống dòng
     */
    public String toProtocolString() {
        return status.code() + (payload.isEmpty() ? "" : " " + payload) + "\n";
    }
}

