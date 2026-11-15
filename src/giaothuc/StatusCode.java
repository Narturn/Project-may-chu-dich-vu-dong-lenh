package giaothuc;

/**
 * Mã trạng thái cấp cao của giao thức phản hồi văn bản.
 */
public enum StatusCode {
    OK,
    ERR;

    /**
     * Lấy mã dạng chuỗi của trạng thái.
     *
     * @return chuỗi biểu diễn mã trạng thái ("OK" hoặc "ERR")
     */
    public String code() {
        return this.name();
    }
}

