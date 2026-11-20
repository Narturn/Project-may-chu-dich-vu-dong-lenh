package nghiepvu;

import giaothuc.CommandException;
import giaothuc.ErrorCode;

import java.util.Locale;
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
        if (args == null || args.isBlank()) {
            throw new CommandException(ErrorCode.ERR_INVALID_ARGUMENT,
                    "Tham số của lệnh UPPER không được để trống");
        }
        return args.toUpperCase(Locale.ROOT);
    }
}

