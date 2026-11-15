package nghiepvu;

import java.util.function.Function;

/**
 * Bộ xử lý lệnh CALC (F13 - Tính năng mở rộng), tính toán giá trị biểu thức số học.
 */
public class CalcHandler implements Function<String, String> {

    public CalcHandler() {
    }

    @Override
    public String apply(String expr) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Đánh giá biểu thức số học theo ký pháp hậu tố (hoặc thuật toán Shunting-yard).
     *
     * @param expr biểu thức số học
     * @return kết quả số thực
     */
    private double evaluatePostfix(String expr) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

