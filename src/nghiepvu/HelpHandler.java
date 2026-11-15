package nghiepvu;

import java.util.function.Function;

/**
 * Bộ xử lý lệnh HELP (F16 - Tính năng mở rộng), liệt kê danh sách lệnh được hỗ trợ từ registry.
 */
public class HelpHandler implements Function<String, String> {

    private final CommandRegistry registry;

    public HelpHandler(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String apply(String args) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

