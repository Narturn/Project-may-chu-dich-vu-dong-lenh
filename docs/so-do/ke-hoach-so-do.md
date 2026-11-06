# KẾ HOẠCH THIẾT KẾ VÀ DANH SÁCH SƠ ĐỒ DỰ ÁN
## ĐỀ TÀI 31: MÁY CHỦ DỊCH VỤ DÒNG LỆNH (COMMAND-LINE SERVICE SERVER)
**Học phần:** Lập trình Java nâng cao (AJP201)  
**Giai đoạn:** Bước 2 trong Kế hoạch triển khai (Thiết kế kiến trúc và Lập danh mục sơ đồ)  
**Căn cứ tài liệu:**
- `reference/Main.pdf`: Mục 1.2 (Luật chung), Mục 1.3 (Rubric chấm), Mục 1.4 (Cấu trúc báo cáo) và Đề tài 31 (trang 18).
- `reference/2.plan-trien-khai-project.md`: Bước 2 (Cài skill drawio, vẽ sơ đồ và setup dự án).
- `docs/danh-sach-chuc-nang.md`: Danh mục 16 chức năng F01–F16 đã được phân tích.
- Slide bài giảng 6 chương lý thuyết (`reference/chuong-01-slide.pdf` đến `reference/chuong-06-slide.pdf`).

> [!IMPORTANT]
> **LƯU Ý VỀ TÌNH TRẠNG TÀI LIỆU:**  
> Các thiết kế kiến trúc sơ bộ, cấu trúc lớp và giải pháp kỹ thuật trong tài liệu này hiện là **CÁC ĐỀ XUẤT DỰ THẢO** để chuẩn bị cho việc vẽ sơ đồ. Các đề xuất này **CHƯA ĐƯỢC NHÓM DUYỆT CHÍNH THỨC** và **CHƯA ĐƯỢC XEM LÀ KIẾN TRÚC CHÍNH THỨC** của dự án. Toàn bộ các đề xuất cần được các thành viên trong nhóm thảo luận, thống nhất trước khi tiến hành vẽ sơ đồ và dựng khung mã nguồn.

---

## I. MỤC TIÊU VÀ NGUYÊN TẮC THIẾT KẾ

Bước 2 là giai đoạn định hình toàn bộ kiến trúc hệ thống trước khi bắt tay vào viết mã nguồn. Mục tiêu chính gồm:
1. **Xác định đầy đủ các sơ đồ bắt buộc:** Tuân thủ đúng danh mục sơ đồ quy định tại Bước 2 của kế hoạch triển khai và cấu trúc báo cáo của đề tài.
2. **Phân tầng kiến trúc rõ ràng:** Đảm bảo tầng nghiệp vụ không phụ thuộc vào hạ tầng mạng TCP để có thể kiểm thử đơn vị độc lập *(Tiêu chí TC3 mức 3)*.
3. **Thảo luận chuẩn biểu diễn kết quả dùng chung:** Đưa ra các phương án biểu diễn kết quả thành công/thất bại xuyên suốt các tầng để nhóm lựa chọn *(Tiêu chí TC3 mức 4)*.
4. **Làm tiền đề cho Bước 3:** Làm cơ sở để sau khi nhóm thống nhất sẽ dựng bộ khung code (package, class, interface, enum, record) và đưa vào báo cáo tổng kết môn học (8–12 trang).

---

## II. DANH SÁCH CÁC SƠ ĐỒ BẮT BUỘC THEO BƯỚC 2 VÀ BÁO CÁO

Theo quy định tại **Bước 2 của `reference/2.plan-trien-khai-project.md`** và **Mục 1.4 của `reference/Main.pdf`**, mọi dự án đều bắt buộc phải có đầy đủ 5 loại sơ đồ nền tảng sau:

```
+-----------------------------------------------------------------------------------+
| 1. Sơ đồ phân rã chức năng (Functional Decomposition Tree) - BẮT BUỘC             |
|    -> Cây phân rã 16 chức năng F01–F16 theo 3 nhóm Lõi / Phải làm / Mở rộng       |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| 2. Sơ đồ kiến trúc các tầng (Layered Architecture Stack) - BẮT BUỘC               |
|    -> Phân tách 3 tầng: Mạng/Giao tiếp -> Nghiệp vụ lõi -> Lưu trữ/Dữ liệu        |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| 3. Sơ đồ tuần tự cho các luồng chính (UML Sequence Diagrams) - BẮT BUỘC           |
|    -> 3.1: Luồng thực thi lệnh tiện ích cơ bản (TIME, UPPER...)                   |
|    -> 3.2: Luồng thống kê dữ liệu đa luồng bằng Streams API (STATS)               |
|    -> 3.3: Luồng phân tích cú pháp và xử lý mã lỗi giao thức                      |
|    -> 3.4: Luồng lưu trữ và khôi phục số liệu ra tệp CSV                          |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| 4. Lưu đồ quy trình phần lõi máy chủ (Core Flowchart) - BẮT BUỘC                  |
|    -> Vòng đời tiếp nhận kết nối, phân luồng worker, xử lý lệnh và giải phóng     |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| 5. Sơ đồ lớp chi tiết hệ thống (UML Class Diagram) - BẮT BUỘC                     |
|    -> Đầy đủ thuộc tính, phương thức, quan hệ, record, enum, custom exception     |
+-----------------------------------------------------------------------------------+
```

---

### 1. Sơ đồ 1: Sơ đồ phân rã chức năng (Functional Decomposition Diagram / Tree)
- **Phân loại:** **Yêu cầu bắt buộc**.
- **Tên sơ đồ:** `so-do-phan-ra-chuc-nang.drawio` (xuất `so-do-phan-ra-chuc-nang.drawio.png`).
- **Mục đích:** Trực quan hóa toàn bộ phạm vi hệ thống dưới dạng cây cấu trúc (tree), thể hiện rõ sự phân định giữa 3 nhóm: Phần lõi, Phải làm, và Mở rộng; làm cơ sở đối chiếu với báo cáo tiến độ và bảng chấm điểm.
- **Những nội dung cần thể hiện:**
  - Gốc: *Hệ thống Máy chủ Dịch vụ Dòng lệnh (Đề tài 31)*.
  - Nhánh 1 - **Phần lõi (Core Framework):** Gồm 5 chức năng `F01` (Mô hình bản tin), `F02` (Mã lỗi & ngoại lệ), `F03` (Phân tích cú pháp văn bản), `F04` (Khung đăng ký Command Registry), `F05` (Máy chủ TCP đa luồng).
  - Nhánh 2 - **Phải làm (Mandatory Business):** Gồm 7 chức năng `F06` (Lệnh TIME/DATE), `F07` (Lệnh ECHO/UPPER), `F08` (Bộ đếm an toàn luồng), `F09` (Lệnh STATS với Streams API), `F10` (Chuẩn hóa phản hồi trạng thái & lỗi), `F11` (Ứng dụng Console Client), `F12` (Lưu trữ số liệu tệp CSV).
  - Nhánh 3 - **Mở rộng (Extensions):** Gồm 4 chức năng `F13` (Lệnh CALC biểu thức), `F14` (Lịch sử HISTORY từng phiên), `F15` (Rate Limiter cửa sổ thời gian), `F16` (Lệnh điều khiển HELP/QUIT).
- **Chức năng liên quan:** Toàn bộ `F01` đến `F16`.
- **Căn cứ tài liệu:** Bước 2 của `reference/2.plan-trien-khai-project.md`, `docs/danh-sach-chuc-nang.md`, Mục 1.3 và Đề 31 của `reference/Main.pdf`.

---

### 2. Sơ đồ 2: Sơ đồ kiến trúc các tầng (Layered Architecture Stack)
- **Phân loại:** **Yêu cầu bắt buộc**.
- **Tên sơ đồ:** `so-do-kien-truc-tang.drawio` (xuất `so-do-kien-truc-tang.drawio.png`).
- **Mục đích:** Thể hiện nguyên tắc thiết kế phân tầng (Layering) theo tiêu chí TC3 mức 3 của Rubric chấm điểm: Tầng giao diện/mạng gọi Tầng nghiệp vụ, Tầng nghiệp vụ gọi Tầng lưu trữ; Tầng nghiệp vụ hoàn toàn không phụ thuộc vào `Socket`, `ServerSocket` hay Console UI để có thể kiểm thử đơn vị độc lập.
- **Những nội dung cần thể hiện:**
  - **Tầng Mạng & Giao tiếp (Network & Presentation Layer):** `CommandServer`, `ClientHandler`, `ConsoleClient`, `ProtocolParser`. Chịu trách nhiệm quản lý kết nối socket, đọc dòng chuỗi, đóng gói chuỗi phản hồi.
  - **Tầng Nghiệp vụ & Điều phối (Core Business & Dispatching Layer):** `CommandRegistry`, `CommandDispatcher`, `MetricsCollector`, các bộ xử lý lệnh (`TimeHandler`, `EchoHandler`, `StatsHandler`, `CalcHandler`...). Chỉ làm việc với đối tượng nghiệp vụ (`Request`, `Response`, `KetQua`), xử lý logic và tính toán.
  - **Tầng Dữ liệu & Lưu trữ (Data & Persistence Layer):** `MetricsRepository`, `CsvStorageManager`. Đảm nhiệm đọc/ghi số liệu thống kê ra tệp CSV, bảo toàn dữ liệu khi khởi động/tắt máy chủ.
  - **Khối Mô hình Dùng chung (Cross-cutting / Shared Model):** `Request`, `Response`, `KetQua` (dự thảo), `ErrorCode`, `CommandException`... được các tầng sử dụng chung.
  - Mũi tên luồng phụ thuộc một chiều từ trên xuống dưới (Network -> Business -> Storage), chỉ rõ Tầng Nghiệp vụ độc lập.
- **Chức năng liên quan:** `F01`, `F03`, `F04`, `F05`, `F08`, `F10`, `F11`, `F12`.
- **Căn cứ tài liệu:** Bước 2 của `reference/2.plan-trien-khai-project.md` ("Tầng nghiệp vụ không phụ thuộc mạng hay giao diện, nhờ vậy kiểm thử được riêng - TC3 mức 3").

---

### 3. Sơ đồ 3: Sơ đồ tuần tự các luồng chính (UML Sequence Diagrams)
- **Phân loại:** **Yêu cầu bắt buộc**.

Theo quy định Bước 2, mỗi luồng chính cần một sơ đồ tuần tự thể hiện sự tương tác giữa các đối tượng qua các tầng:

#### 3.1. Sơ đồ 3.1: Luồng thực thi lệnh tiện ích cơ bản (ví dụ TIME hoặc UPPER)
- **Tên sơ đồ:** `so-do-tuan-tu-lenh-co-ban.drawio` (xuất `so-do-tuan-tu-lenh-co-ban.drawio.png`).
- **Mục đích:** Thể hiện vòng đời xử lý hoàn chỉnh của một lệnh thông thường từ lúc client nhập văn bản đến khi nhận được chuỗi kết quả `OK ...`.
- **Những nội dung cần thể hiện:**
  - Các đối tượng tham gia (Lifelines): `ClientUser` -> `ConsoleClient` -> `Socket` -> `ClientHandler` -> `ProtocolParser` -> `CommandDispatcher` -> `CommandRegistry` -> `Function<String, String>` (cụ thể: `UpperHandler` hoặc `TimeHandler`) -> `MetricsCollector`.
  - Các thông điệp tuần tự: Gửi chuỗi thô -> Đọc dòng -> Phân tích cú pháp thành `Request` -> Tra cứu trong `Map` -> Thực thi Lambda/Method reference -> Tăng bộ đếm an toàn luồng -> Đóng gói `Response.ok(...)` -> Gửi chuỗi về Socket -> Hiển thị màn hình console.
- **Chức năng liên quan:** `F01`, `F03`, `F04`, `F05`, `F06`, `F07`, `F08`, `F10`, `F11`.
- **Căn cứ tài liệu:** Đề 31 `reference/Main.pdf` (Ứng dụng mạng: TIME, ECHO, UPPER; Khung Map<String, Function>).

#### 3.2. Sơ đồ 3.2: Luồng thống kê dữ liệu đa luồng bằng Streams API (lệnh STATS)
- **Tên sơ đồ:** `so-do-tuan-tu-lenh-stats.drawio` (xuất `so-do-tuan-tu-lenh-stats.drawio.png`).
- **Mục đích:** Thể hiện cách thức lệnh `STATS` truy xuất an toàn dữ liệu từ bộ đếm đa luồng và áp dụng Streams API để tổng hợp, sắp xếp và phản hồi.
- **Những nội dung cần thể hiện:**
  - Các đối tượng tham gia: `ClientHandler` -> `CommandDispatcher` -> `StatsHandler` -> `MetricsCollector` -> `Java Streams API` (snapshot, filter, groupingBy, sorted, collect).
  - Trình bày rõ việc lấy bản sao chụp (snapshot) an toàn luồng từ `ConcurrentHashMap` / `AtomicInteger` trước khi mở pipeline Stream nhằm tránh ngoại lệ tranh chấp ghi/đọc đồng thời.
- **Chức năng liên quan:** `F04`, `F08`, `F09`, `F10`.
- **Căn cứ tài liệu:** Đề 31 `reference/Main.pdf` (Dữ liệu và Streams: "STATS đếm kết nối và lệnh theo loại bằng Streams", Đồng thời: "bộ đếm an toàn luồng").

#### 3.3. Sơ đồ 3.3: Luồng phân tích cú pháp và xử lý mã lỗi giao thức
- **Tên sơ đồ:** `so-do-tuan-tu-xu-ly-loi.drawio` (xuất `so-do-tuan-tu-xu-ly-loi.drawio.png`).
- **Mục đích:** Làm rõ cơ chế bắt lỗi và trả về mã lỗi chuẩn (`ERR <mã_lỗi> <thông_báo>`) khi người dùng nhập sai cú pháp, gọi lệnh không tồn tại, hoặc khi hàm xử lý gặp lỗi runtime (ví dụ chia cho 0).
- **Những nội dung cần thể hiện:**
  - Tình huống lỗi 1: `ProtocolParser` phát hiện cú pháp rỗng / sai -> Ném `CommandSyntaxException` -> `ClientHandler` bắt và chuyển thành `Response.err(ERR_SYNTAX, "...")`.
  - Tình huống lỗi 2: `CommandDispatcher` không tìm thấy lệnh trong `Map` -> Chuyển thành `Response.err(ERR_UNKNOWN_COMMAND, "...")`.
  - Tình huống lỗi 3: Ngoại lệ không mong muốn trong hàm -> Bắt an toàn, chuyển thành `Response.err(ERR_INTERNAL, "...")` mà không ngắt socket.
- **Chức năng liên quan:** `F01`, `F02`, `F03`, `F04`, `F10`.
- **Căn cứ tài liệu:** Đề 31 `reference/Main.pdf` ("lỗi trả mã và thông báo"), slide Chương 6 (Custom exceptions).

#### 3.4. Sơ đồ 3.4: Luồng nạp và lưu trữ bền vững số liệu (Persistence)
- **Tên sơ đồ:** `so-do-tuan-tu-luu-tru.drawio` (xuất `so-do-tuan-tu-luu-tru.drawio.png`).
- **Mục đích:** Thể hiện quy trình nạp số liệu lịch sử từ tệp CSV khi máy chủ khởi động và quy trình lưu vết số liệu ra tệp khi máy chủ dừng hoạt động (Shutdown Hook).
- **Những nội dung cần thể hiện:**
  - Luồng 1 (Startup): `CommandServer.start()` -> `MetricsRepository.loadFromCsv()` -> đọc tệp bằng `try-with-resources` (`BufferedReader`) -> khôi phục trạng thái cho `MetricsCollector`.
  - Luồng 2 (Shutdown): `JVM ShutdownHook` / `CommandServer.stop()` -> `MetricsCollector.exportSnapshot()` -> `MetricsRepository.saveToCsv()` -> ghi tệp bằng `try-with-resources` (`PrintWriter`).
- **Chức năng liên quan:** `F08`, `F12`.
- **Căn cứ tài liệu:** Mục 1.2 `reference/Main.pdf` ("Dữ liệu lưu bằng tệp văn bản, CSV hoặc tuần tự hóa"), slide Chương 5, Chương 6 (`try-with-resources`).

---

### 4. Sơ đồ 4: Lưu đồ quy trình phần lõi máy chủ (Core Flowchart)
- **Phân loại:** **Yêu cầu bắt buộc**.
- **Tên sơ đồ:** `luu-do-quy-trinh-phan-loi.drawio` (xuất `luu-do-quy-trinh-phan-loi.drawio.png`).
- **Mục đích:** Trực quan hóa chi tiết thuật toán điều khiển của vòng lặp tiếp nhận socket và chu trình sống của một kết nối máy khách trong máy chủ đa luồng.
- **Những nội dung cần thể hiện:**
  - Khối Bắt đầu: Khởi tạo `ServerSocket` trên cổng cấu hình.
  - Vòng lặp chính máy chủ: Chờ kết nối (`serverSocket.accept()`) -> Tiếp nhận `Socket` client -> Chuyển `Socket` cho luồng Worker (Thread-per-connection hoặc Thread Pool `ExecutorService`).
  - Vòng lặp Worker xử lý kết nối:
    + Đọc dòng văn bản (`readLine()`).
    + Điều kiện dừng: Dòng đọc được là `null` (EOF / Client ngắt kết nối) hoặc lệnh `QUIT` -> Tiến hành đóng socket an toàn (`try-with-resources`), giảm bộ đếm kết nối đang hoạt động -> Kết thúc luồng worker.
    + Kiểm tra Rate Limiting (nếu kích hoạt): Vượt ngưỡng -> Trả `ERR_RATE_LIMIT_EXCEEDED` -> Quay lại vòng lặp.
    + Phân tích cú pháp dòng lệnh (`parse`): Cú pháp sai -> Trả `ERR_SYNTAX` -> Quay lại vòng lặp.
    + Tra cứu Registry: Tên lệnh không có -> Trả `ERR_UNKNOWN_COMMAND` -> Quay lại vòng lặp.
    + Thực thi `Function<String, String>`: Tăng bộ đếm an toàn luồng (`MetricsCollector`), lưu `History` phiên -> Đóng gói phản hồi `OK <kết_quả>` -> Ghi ra Socket (`flush()`) -> Quay lại vòng lặp đọc lệnh kế tiếp.
- **Chức năng liên quan:** `F01`, `F02`, `F03`, `F04`, `F05`, `F08`, `F10`, `F15`, `F16`.
- **Căn cứ tài liệu:** Bước 2 `reference/2.plan-trien-khai-project.md` ("Lưu đồ (flowchart) cho phần lõi của đề"), slide Chương 4, Chương 5.

---

### 5. Sơ đồ 5: Sơ đồ lớp chi tiết hệ thống (UML Class Diagram)
- **Phân loại:** **Yêu cầu bắt buộc**.
- **Tên sơ đồ:** `so-do-lop-uml.drawio` (xuất `so-do-lop-uml.drawio.png`).
- **Mục đích:** Tổng hợp toàn bộ cấu trúc lớp, giao diện, record, enum và các mối quan hệ (kế thừa, phụ thuộc, kết tập, hiện thực hóa) của toàn bộ hệ thống; làm bản thiết kế chi tiết để lập trình viên tạo mã khung ở Bước 3.
- **Những nội dung dự thảo cần thể hiện (đang chờ nhóm duyệt):**
  - **Nhóm Giao tiếp & Mạng (package `mang`):**
    + `CommandServer`: `port: int`, `isRunning: boolean`, `serverSocket: ServerSocket`, cơ chế đa luồng (Thread-per-connection hoặc `ExecutorService`), `start()`, `stop()`.
    + `ClientHandler implements Runnable`: `clientSocket: Socket`, `reader: BufferedReader`, `writer: PrintWriter`, `dispatcher: CommandDispatcher`, `metrics: MetricsCollector`, `run()`.
    + `ConsoleClient`: `host: String`, `port: int`, `socket: Socket`, `connect()`, `sendRequest()`, `disconnect()`.
  - **Nhóm Phân tích & Giao thức (package `giaothuc`):**
    + `ProtocolParser`: `parse(String rawLine): Request`.
    + `record Request(String command, String arguments, LocalDateTime timestamp)`: Bất biến (Ch6).
    + `record Response(StatusCode status, String data, String errorMessage)`: Bất biến (Ch6).
    + `enum StatusCode`: `OK`, `ERR`...
    + `enum ErrorCode`: `ERR_UNKNOWN_COMMAND`, `ERR_SYNTAX`, `ERR_INTERNAL`, `ERR_RATE_LIMIT`... có mã và mô tả (Ch6).
    + `sealed interface KetQua permits ThanhCong, ThatBai` (Phương án đề xuất dùng chung).
    + `class CommandException extends RuntimeException` (Phương án đề xuất ngoại lệ riêng).
  - **Nhóm Nghiệp vụ & Điều phối (package `nghiepvu`):**
    + `CommandRegistry`: `handlers: Map<String, Function<String, String>>`, `register(String, Function)`, `getHandler(String): Optional<Function>`.
    + `CommandDispatcher`: `execute(Request): Response` (hoặc trả về `KetQua`).
    + Các lớp chức năng tiện ích: `TimeService`, `StringService`, `MathExpressionEvaluator` (`CALC`), `SessionHistoryManager` (`HISTORY`), `RateLimiter`.
    + `MetricsCollector`: Quản lý bộ đếm in-memory an toàn luồng, cung cấp pipeline Stream cho `STATS`.
  - **Nhóm Dữ liệu & Lưu trữ (package `luutru`):**
    + `MetricsRepository`: `csvPath: Path`, `saveMetrics(MetricsCollector)`, `loadMetrics(): MetricsCollector`.
  - Mối quan hệ UML chuẩn xác: Dependency (`..>`), Realization (`..|>`), Association (`-->`), Composition/Aggregation (`--*` / `--o`).
- **Chức năng liên quan:** Toàn bộ `F01` đến `F16`.
- **Căn cứ tài liệu:** Bước 2 `reference/2.plan-trien-khai-project.md`, Mục 1.2, 1.4 `reference/Main.pdf`, slide bài giảng 6 chương.

---

## III. DANH SÁCH CÁC SƠ ĐỒ ĐỀ XUẤT THÊM (KHÔNG BẮT BUỘC)

> [!NOTE]
> Đây là các **ĐỀ XUẤT BỔ SUNG**, không nằm trong danh mục bắt buộc tối thiểu của Bước 2 nhưng rất hữu ích cho bài báo cáo và buổi thuyết trình bảo vệ trước giảng viên nhằm nâng cao điểm ở Tiêu chí 4 (Báo cáo) và Tiêu chí 6 (Bảo vệ).

1. **Đề xuất 1: Sơ đồ kiến trúc mạng Client-Server (Deployment / Network Topology Diagram)**
   - *Tên sơ đồ:* `so-do-kien-truc-mang.drawio` (xuất `.png`).
   - *Mục đích:* Thể hiện trực quan mô hình kết nối vật lý/mạng giữa các máy khách `ConsoleClient` qua giao thức TCP/IP tới tiến trình `CommandServer`, cơ chế phân bổ Socket, luồng I/O văn bản UTF-8 và tệp lưu trữ dữ liệu CSV trên đĩa cứng.
   - *Căn cứ:* Mục 1.4 `reference/Main.pdf` ("sơ đồ client-server nếu đề có").

2. **Đề xuất 2: Sơ đồ máy trạng thái phiên làm việc của Client (Client Session State Machine Diagram)**
   - *Tên sơ đồ:* `so-do-trang-thai-phien.drawio` (xuất `.png`).
   - *Mục đích:* Thể hiện các trạng thái của một kết nối từ góc nhìn máy chủ: `CONNECTED` -> `WAITING_FOR_COMMAND` -> `VALIDATING_RATE_LIMIT` -> `EXECUTING` -> `SENDING_RESPONSE` -> `DISCONNECTED` (khi gặp `QUIT` hoặc ngắt kết nối). Rất hữu ích khi bảo vệ tính năng mở rộng `RateLimiter` (F15) và `SessionHistory` (F14).

---

## IV. THỨ TỰ THIẾT KẾ VÀ QUAN HỆ PHỤ THUỘC GIỮA CÁC SƠ ĐỒ

Để quá trình thiết kế diễn ra mạch lạc, không phải sửa đi sửa lại nhiều lần, thứ tự thực hiện được quy định như sau:

```
[1. Sơ đồ phân rã chức năng]
             |
             v
[2. Sơ đồ kiến trúc các tầng]
             |
             v
[3. Các sơ đồ tuần tự luồng chính] (3.1, 3.2, 3.3, 3.4)
             |
             v
[4. Lưu đồ quy trình phần lõi]
             |
             v
[5. Sơ đồ lớp chi tiết UML]
             |
             v
[Các sơ đồ đề xuất thêm: Kiến trúc mạng & Máy trạng thái] (Nếu nhóm duyệt thực hiện)
```

### Giải thích sự phụ thuộc:
1. **Sơ đồ phân rã chức năng (1) làm đầu tiên:** Cần có bức tranh tổng thể về 16 chức năng để không bỏ sót bất kỳ yêu cầu nào của đề tài.
2. **Sơ đồ kiến trúc các tầng (2) làm thứ hai:** Dựa trên các chức năng đã phân rã, ta phân bổ chúng vào 3 tầng kiến trúc (Mạng, Nghiệp vụ, Lưu trữ) để thiết lập ranh giới trách nhiệm và đảm bảo nguyên tắc độc lập của tầng nghiệp vụ.
3. **Các sơ đồ tuần tự (3) làm thứ ba:** Khi đã có các tầng, ta vẽ tuần tự từng luồng tương tác thực tế giữa các đối tượng để xác định chính xác các đối tượng cần giao tiếp với nhau bằng những phương thức (method call) nào.
4. **Lưu đồ quy trình phần lõi (4) làm thứ tư:** Chi tiết hóa thuật toán lặp, điều kiện kiểm tra, bắt lỗi và rẽ nhánh của ServerSocket và Worker.
5. **Sơ đồ lớp chi tiết UML (5) làm sau cùng:** Sau khi toàn bộ các luồng tuần tự và thuật toán đã được xác lập, sơ đồ lớp UML mới có đầy đủ cơ sở chính xác về danh sách thuộc tính, kiểu dữ liệu, chữ ký hàm, kiểu trả về và các đường quan hệ, tránh việc phải chỉnh sửa cấu trúc lớp nhiều lần.

---

## V. CÁC NỘI DUNG KỸ THUẬT ĐỀ XUẤT CẦN NHÓM THẢO LUẬN VÀ THỐNG NHẤT

Trước khi sử dụng `drawio` để vẽ sơ đồ lớp và sơ đồ tuần tự, nhóm cần thảo luận và biểu quyết thống nhất 5 nội dung kỹ thuật trọng yếu sau:

### 1. Đề xuất cách biểu diễn kết quả thành công / thất bại dùng chung (TC3 mức 4)
- **Tình trạng:** **Đề xuất kỹ thuật - Đang chờ nhóm thảo luận và thống nhất**.
- **Yêu cầu từ tài liệu:** `reference/2.plan-trien-khai-project.md` yêu cầu chọn 1 trong 4 cách: `boolean`, `ném ngoại lệ`, `Optional`, hoặc `sealed interface KetQua`.
- **Phương án đề xuất:** Nhóm nên cân nhắc **Phương án `sealed interface KetQua`**:
  ```java
  public sealed interface KetQua permits KetQua.ThanhCong, KetQua.ThatBai {
      record ThanhCong(String duLieu) implements KetQua {}
      record ThatBai(ErrorCode maLoi, String thongDiep) implements KetQua {}
  }
  ```
  *Lý do đề xuất:* Vừa thỏa mãn tiêu chí `sealed` và `record` của Chương 6, vừa giúp câu lệnh `switch` ở tầng điều phối có thể kiểm tra đầy đủ mọi trường hợp (exhaustive matching) mà không cần nhánh `default`, hướng tới mức điểm tối đa trong Rubric (TC3 mức 4-5).

---

### 2. Đề xuất cơ chế xử lý lỗi với `Map<String, Function<String, String>>`
- **Tình trạng:** **Đề xuất kỹ thuật - Đang chờ nhóm thảo luận và thống nhất**.
- **Yêu cầu từ đề bài Đ31:** Bắt buộc cấu trúc `Map<String, Function<String, String>>`.
- **Phương án đề xuất:**
  - Định nghĩa ngoại lệ riêng `CommandException extends RuntimeException` (mang theo `ErrorCode`).
  - Các hàm Lambda trong `Map` ném `CommandException` khi gặp lỗi tham số/nghiệp vụ.
  - Tầng `CommandDispatcher` bọc khối `try-catch (CommandException e)` để chuyển đổi thành `KetQua.ThatBai`.
  - *Chi tiết phân tích xem tại `docs/so-do/ra-soat-thiet-ke.md`.*

---

### 3. Đề xuất quy chuẩn định dạng giao thức văn bản một dòng (Single-line Protocol)
- **Tình trạng:** **Đề xuất kỹ thuật - Đang chờ nhóm thảo luận và thống nhất**.
- **Mục tiêu:** Tránh lỗi ngắt dòng dữ liệu socket (`line framing`) giữa client và server.
- **Phương án đề xuất:**
  - Chuỗi Client gửi lên: `<TÊN_LỆNH>[ <THAM_SỐ>]\n`
  - Chuỗi Server phản hồi:
    - Khi thành công: `OK <NỘI_DUNG_KẾT_QUẢ>\n`
    - Khi có lỗi: `ERR <MÃ_LỖI> <THÔNG_BÁO_CHI_TIẾT>\n`
  - Danh mục mã lỗi cơ bản: `ERR_UNKNOWN_COMMAND`, `ERR_SYNTAX`, `ERR_INVALID_ARGUMENT`, `ERR_INTERNAL`, `ERR_RATE_LIMIT`.

---

### 4. Đề xuất mô hình đa luồng tại Server (Concurrency Model)
- **Tình trạng:** **Đề xuất kỹ thuật - Đang chờ nhóm thảo luận và thống nhất**.
- **Hai phương án cân nhắc:**
  - *Phương án A:* `Thread-per-connection` truyền thống (`new Thread(new ClientHandler(socket)).start()`) theo Chương 5 slide 23.
  - *Phương án B (Đề xuất):* Dùng `ExecutorService threadPool = Executors.newFixedThreadPool(10)` từ Chương 4 slide 30 và Chương 5 slide 24, kết hợp đặt `socket.setSoTimeout(60_000)`.
- **Lưu ý kỹ thuật quan trọng về hàng đợi của `newFixedThreadPool`:**
  - `Executors.newFixedThreadPool(10)` có **số worker tối đa xử lý đồng thời là 10 luồng**.
  - Nó sử dụng hàng đợi **`LinkedBlockingQueue` không giới hạn kích thước** (`Integer.MAX_VALUE`) để chứa các tác vụ chờ.
  - Trong máy chủ Socket, vì mỗi `ClientHandler` giữ kết nối suốt phiên làm việc của client, 10 worker sẽ phục vụ tối đa 10 client hoạt động cùng lúc. Kết nối thứ 11 trở đi sau khi được `accept()` sẽ bị xếp vào hàng đợi chờ và **chưa được phục vụ cho đến khi có một kết nối trước đó đóng lại**.
  - Do đó, bắt buộc phải kết hợp cơ chế `setSoTimeout` hoặc giới hạn thời gian nhàn rỗi (idle) để tránh tình trạng cạn kiệt worker làm tắc nghẽn các tác vụ trong hàng đợi.

---

### 5. Đề xuất cấu trúc tệp lưu trữ thống kê (`metrics.csv`)
- **Tình trạng:** **Đề xuất kỹ thuật - Đang chờ nhóm thảo luận và thống nhất**.
- **Yêu cầu bắt buộc:** Mục 1.2 `reference/Main.pdf` quy định dữ liệu lưu bằng tệp văn bản / CSV.
- **Phương án đề xuất:**
  - Đường dẫn tệp mặc định: `data/metrics.csv`.
  - Cấu trúc các dòng CSV gồm 4 cột: `MetricType,Key,Value,LastUpdated`.
  ```csv
  MetricType,Key,Value,LastUpdated
  CONNECTION,TOTAL,142,2026-11-06T12:00:00
  COMMAND,TIME,45,2026-11-06T12:05:10
  COMMAND,DATE,20,2026-11-06T12:05:12
  COMMAND,UPPER,35,2026-11-06T12:06:00
  COMMAND,ECHO,22,2026-11-06T12:06:30
  COMMAND,STATS,20,2026-11-06T12:07:00
  ```

---

## VI. BẢNG CHECKLIST DUYỆT KẾ HOẠCH TRƯỚC KHI VẼ

| STT | Nội dung | Phân loại | Tình trạng hiện tại | Ghi chú của nhóm |
|---|---|---|---|---|
| 1 | Danh sách 5 sơ đồ cốt lõi (Phân rã, Tầng kiến trúc, Tuần tự, Lưu đồ lõi, Lớp UML) | Yêu cầu bắt buộc | Đã xác định theo Bước 2 | Chờ nhóm duyệt danh mục |
| 2 | Danh sách 2 sơ đồ mở rộng (Kiến trúc mạng Client-Server, Máy trạng thái phiên) | Đề xuất bổ sung | Đề xuất tăng điểm báo cáo | Chờ nhóm quyết định có làm hay không |
| 3 | Thứ tự triển khai: Phân rã -> Kiến trúc tầng -> Tuần tự -> Lưu đồ lõi -> Lớp UML | Quy trình kỹ thuật | Đề xuất hợp lý | Chờ nhóm duyệt quy trình |
| 4 | Kiểu kết quả chung: `sealed interface KetQua` (Thành công / Thất bại) | Đề xuất kỹ thuật | **Chờ nhóm thống nhất** | Đề xuất tối ưu cho TC3 mức 4-5 |
| 5 | Cơ chế bọc lỗi `CommandException extends RuntimeException` cho `Function` | Đề xuất kỹ thuật | **Chờ nhóm thống nhất** | Đảm bảo không lỗi biên dịch Lambda |
| 6 | Định dạng giao thức văn bản một dòng (`OK ...` / `ERR <mã> <thông điệp>`) | Đề xuất kỹ thuật | **Chờ nhóm thống nhất** | Đảm bảo không vỡ luồng socket |
| 7 | Mô hình đa luồng: `ExecutorService` (Fixed Pool) vs `Thread-per-connection` | Đề xuất kỹ thuật | **Chờ nhóm thống nhất** | Lưu ý cơ chế hàng đợi và worker |
| 8 | Cấu trúc tệp dữ liệu bền vững `metrics.csv` | Đề xuất kỹ thuật | **Chờ nhóm thống nhất** | Đáp ứng yêu cầu lưu trữ tệp Mục 1.2 |
