package giaothuc;

/**
 * Thành phần phân tích cú pháp chuỗi văn bản dòng lệnh từ Socket thành đối tượng {@link Request}.
 */
public class ProtocolParser {

    /**
     * Độ dài dòng tối đa cho phép để tránh tấn công làm cạn kiệt bộ nhớ.
     */
    public static final int MAX_LINE_LENGTH = 4096;

    public ProtocolParser() {
    }

    /**
     * Phân tích một dòng chuỗi thô từ socket thành đối tượng Request.
     *
     * @param rawLine dòng văn bản nhận từ socket
     * @return đối tượng {@link Request} đã qua chuẩn hóa
     * @throws CommandException khi cú pháp không hợp lệ hoặc dòng quá dài
     */
    public Request parse(String rawLine) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Chuẩn hóa dòng văn bản (cắt tỉa khoảng trắng, chuẩn hóa định dạng).
     *
     * @param rawLine dòng văn bản thô
     * @return chuỗi đã chuẩn hóa
     */
    private String normalize(String rawLine) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

