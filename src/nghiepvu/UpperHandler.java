package nghiepvu;

import giaothuc.CommandException;
import java.util.function.Function;

/**
 * Bộ xử lý lệnh UPPER (F07), chuyển đổi toàn bộ chuỗi tham số thành chữ hoa.
 * Ném {@link CommandException} nếu tham số rỗng.
 */
public class UpperHandler implements Function<String, String> {

    public UpperHandler() {
    }

    @Override
    public String apply(String args) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

