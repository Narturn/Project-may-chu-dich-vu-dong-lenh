package nghiepvu;

import java.util.function.Function;

/**
 * Bộ xử lý lệnh ECHO (F07), phản hồi lại nguyên văn chuỗi tham số nhận được.
 */
public class EchoHandler implements Function<String, String> {

    public EchoHandler() {
    }

    @Override
    public String apply(String args) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

