package giaothuc;

import java.util.Locale;

/**
 * Thành phần phân tích cú pháp chuỗi văn bản dòng lệnh từ Socket thành đối tượng {@link Request} (F03).
 */
public class ProtocolParser {

    /**
     * Độ dài nội dung dòng tối đa theo String.length(), không gồm CR/LF kết thúc.
     * Tầng mạng cần giới hạn ngay khi đọc để tránh giữ dòng quá lớn trong bộ nhớ.
     */
    public static final int MAX_LINE_LENGTH = 4096;

    public ProtocolParser() {
    }

    /**
     * Phân tích một dòng chuỗi thô từ socket thành đối tượng Request.
     * Tên lệnh chuyển thành chữ hoa; giữ nguyên nội dung tham số sau khi bỏ
     * khoảng trắng đầu/cuối dòng và các ký tự phân cách trước tham số.
     * Việc kiểm tra lệnh được hỗ trợ thuộc trách nhiệm của Dispatcher.
     *
     * @param rawLine dòng văn bản nhận từ socket
     * @return đối tượng {@link Request} đã qua chuẩn hóa
     * @throws CommandException khi cú pháp không hợp lệ hoặc dòng quá dài
     */
    public Request parse(String rawLine) {
        String line = normalize(rawLine);
        int commandEnd = 0;
        while (commandEnd < line.length() && !Character.isWhitespace(line.charAt(commandEnd))) {
            commandEnd++;
        }
        int argumentsStart = commandEnd;
        while (argumentsStart < line.length() && Character.isWhitespace(line.charAt(argumentsStart))) {
            argumentsStart++;
        }
        String command = line.substring(0, commandEnd).toUpperCase(Locale.ROOT);
        return Request.of(command, line.substring(argumentsStart));
    }

    /**
     * Bỏ tối đa một dấu kết thúc dòng, kiểm tra độ dài trước khi cắt khoảng trắng.
     * BufferedReader.readLine() đã bỏ dấu kết thúc; vẫn hỗ trợ đầu vào LF/CRLF trực tiếp.
     */
    private String normalize(String rawLine) {
        if (rawLine == null) {
            throw new CommandException(ErrorCode.ERR_SYNTAX, "Dòng lệnh không được null");
        }
        int end = rawLine.length();
        if (rawLine.endsWith("\r\n")) {
            end -= 2;
        } else if (rawLine.endsWith("\n") || rawLine.endsWith("\r")) {
            end--;
        }
        if (end > MAX_LINE_LENGTH) {
            throw new CommandException(ErrorCode.ERR_SYNTAX,
                    "Dòng lệnh vượt quá giới hạn " + MAX_LINE_LENGTH + " ký tự");
        }
        String line = rawLine.substring(0, end);
        if (line.contains("\r") || line.contains("\n")) {
            throw new CommandException(ErrorCode.ERR_SYNTAX, "Mỗi yêu cầu chỉ được chứa một dòng lệnh");
        }
        line = line.strip();
        if (line.isEmpty()) {
            throw new CommandException(ErrorCode.ERR_SYNTAX, "Dòng lệnh không được rỗng");
        }
        return line;
    }
}
