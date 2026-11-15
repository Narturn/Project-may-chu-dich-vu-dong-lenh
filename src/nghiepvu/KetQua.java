package nghiepvu;

import giaothuc.ErrorCode;

/**
 * Kiểu kết quả nội bộ dùng chung của tầng nghiệp vụ, biểu diễn dưới dạng sealed interface
 * gồm hai trạng thái: Thành công (mang dữ liệu) hoặc Thất bại (mang mã lỗi và thông điệp).
 */
public sealed interface KetQua permits KetQua.ThanhCong, KetQua.ThatBai {

    /**
     * Kết quả xử lý nghiệp vụ thành công.
     *
     * @param duLieu chuỗi dữ liệu kết quả tính toán hoặc phản hồi
     */
    record ThanhCong(String duLieu) implements KetQua {
    }

    /**
     * Kết quả xử lý nghiệp vụ thất bại.
     *
     * @param maLoi    mã lỗi hệ thống chuẩn {@link ErrorCode}
     * @param thongDiep thông điệp lỗi chi tiết
     */
    record ThatBai(ErrorCode maLoi, String thongDiep) implements KetQua {
    }
}

