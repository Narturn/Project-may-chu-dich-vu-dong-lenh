# BÁO CÁO RÀ SOÁT TÍNH NHẤT QUÁN THIẾT KẾ KỸ THUẬT
## ĐỀ TÀI 31: MÁY CHỦ DỊCH VỤ DÒNG LỆNH (COMMAND-LINE SERVICE SERVER)
**Tài liệu rà soát:** `docs/so-do/ke-hoach-so-do.md`  
**Mục đích:** Đánh giá tính khả thi và nhất quán kỹ thuật trên 4 vấn đề trọng yếu trước khi tiến hành vẽ sơ đồ chi tiết.  
**Nguyên tắc:** Giữ nguyên danh mục sơ đồ và danh sách chức năng F01–F16; không thay đổi yêu cầu đề bài; đưa ra các phương án rõ ràng để nhóm thảo luận và thống nhất.

> [!IMPORTANT]
> **LƯU Ý VỀ TÌNH TRẠNG BÁO CÁO (CẬP NHẬT SAU KHI NHÓM HỌP THỐNG NHẤT):**  
> Báo cáo này tổng hợp các phân tích kỹ thuật và đối sánh các phương án thiết kế trên 4 vấn đề trọng yếu của dự án. **CẢ 4 QUYẾT ĐỊNH KỸ THUẬT TRONG TÀI LIỆU NÀY ĐÃ ĐƯỢC NHÓM THẢO LUẬN VÀ THỐNG NHẤT CHÍNH THỨC**, trở thành thiết kế chuẩn của dự án để đồng bộ toàn bộ sơ đồ và triển khai mã nguồn Java. Các phương án thay thế không được chọn được giữ lại trong bảng phân tích để phục vụ lập luận bảo vệ đồ án và được đánh dấu rõ là **Không sử dụng**.

---

## 1. VẤN ĐỀ 1: SỬ DỤNG `CommandException` VỚI `Function<String, String>`
- **Tình trạng:** **Quyết định chính thức - Đã được nhóm thống nhất**.

### 1.1. Hiện trạng
- Đề bài Đề tài 31 (`reference/Main.pdf`, trang 18) quy định bắt buộc:  
  *“Bộ xử lý trong `Map<String, Function<String, String>>`”*.
- Trong thư viện chuẩn Java (`java.util.function.Function<T, R>`), phương thức trừu tượng là:
  ```java
  R apply(T t);
  ```
  Phương thức này **không khai báo `throws` bất kỳ checked exception nào**.
- Trong `docs/so-do/ke-hoach-so-do.md`, thiết kế ban đầu dự kiến: các bộ xử lý lệnh khi gặp lỗi nghiệp vụ (ví dụ chia cho 0 trong lệnh `CALC`, thiếu tham số trong `UPPER`) sẽ ném ngoại lệ tự định nghĩa `CommandException`.
- Đồng thời, Luật chung Mục 1.2 `reference/Main.pdf` và Chương 6 slide 20 yêu cầu có ít nhất một ngoại lệ tự định nghĩa (`custom exception`), thường được minh họa bằng checked exception:
  ```java
  public class KhongDuDiemException extends Exception { ... }
  ```

### 1.2. Vấn đề phát sinh
Nếu ta định nghĩa `CommandException extends Exception` (tức là một **Checked Exception**), trình biên dịch Java sẽ **báo lỗi biên dịch ngay lập tức** khi viết biểu thức Lambda hoặc Method reference cho `Function<String, String>`:
```java
// LỖI BIÊN DỊCH: Unhandled exception type CommandException
Function<String, String> calcHandler = arg -> {
    if (arg.contains("/ 0")) {
        throw new CommandException("Divide by zero"); // Java không cho phép!
    }
    return evaluate(arg);
};
```
Lý do: Java cấm Lambda ném checked exception nếu functional interface mục tiêu không khai báo `throws` ngoại lệ đó.

### 1.3. Các phương án giải quyết

| Tiêu chí | Phương án 1A: `CommandException` là `RuntimeException` **[ĐÃ CHỌN]** | Phương án 1B: Tạo Custom Interface kèm Wrapper **[KHÔNG SỬ DỤNG]** | Phương án 1C: Đổi kiểu `Function<String, KetQua>` **[KHÔNG SỬ DỤNG]** |
|---|---|---|---|
| **Cơ chế** | `public class CommandException extends RuntimeException` (Unchecked Exception). | Tạo `@FunctionalInterface ThrowingHandler` có `throws CommandException`, sau đó viết hàm tiện ích `wrap(...)` để bọc sang `Function<String, String>`. | Đổi `Map` thành `Map<String, Function<String, KetQua>>`. |
| **Tuân thủ đề bài** | **Đúng 100% chữ ký đề bài** (`Map<String, Function<String, String>>`). | Đúng chữ ký ngoài `Map`, nhưng sinh thêm 1 tầng interface trung gian. | **Vi phạm đề bài** (đề bài yêu cầu `Function<String, String>`). |
| **Độ phức tạp mã** | Rất thấp, Lambda viết tự nhiên, trong sáng. | Trung bình (cần hàm wrapper chuyển đổi). | Thấp. |
| **Tuân thủ Ch6** | Đáp ứng tốt (Chương 6 slide 19 cho phép cả Checked và Unchecked). | Đáp ứng tốt. | Không dùng exception cho luồng lỗi. |

### 1.4. Quyết định chính thức của nhóm
> [!IMPORTANT]
> **QUYẾT ĐỊNH 1 - ĐÃ ĐƯỢC NHÓM THỐNG NHẤT: Chọn Phương án 1A (`CommandException extends RuntimeException`)**:
> 1. Định nghĩa `CommandException` kế thừa `RuntimeException`, bên trong mang theo `ErrorCode` và thông điệp lỗi.
> 2. Các bộ xử lý Lambda (`Function<String, String>`) được phép ném `CommandException` mà không vi phạm quy tắc biên dịch của Java, giữ nguyên chữ ký `Map<String, Function<String, String>>` của đề bài.
> 3. **`CommandDispatcher` chịu trách nhiệm bắt và chuyển đổi ngoại lệ** thành `KetQua.ThatBai` (xem Quyết định 2):
>    ```java
>    try {
>        String result = handler.apply(request.arguments());
>        return new KetQua.ThanhCong(result);
>    } catch (CommandException e) {
>        return new KetQua.ThatBai(e.getErrorCode(), e.getMessage());
>    } catch (Exception e) {
>        return new KetQua.ThatBai(ErrorCode.ERR_INTERNAL, "Lỗi máy chủ nội bộ");
>    }
>    ```
> Giải pháp này vừa tuân thủ tuyệt đối chữ ký `Map<String, Function<String, String>>` của đề bài, vừa xử lý lỗi an toàn không làm sập luồng worker.
>
> *Phương án 1B và 1C:* **Không sử dụng** (giữ lại trong bảng 1.3 chỉ để làm căn cứ lập luận khi bảo vệ).

---

## 2. VẤN ĐỀ 2: PHÂN BIỆT TRÁCH NHIỆM GIỮA `KetQua`, `Response`, `StatusCode` VÀ `ErrorCode`
- **Tình trạng:** **Quyết định chính thức - Đã được nhóm thống nhất (Phương án 2B)**.

### 2.1. Hiện trạng
Trong tài liệu thiết kế xuất hiện 4 khái niệm liên quan đến kết quả và trạng thái:
1. `KetQua`: Kiểu kết quả dùng chung đang được đề xuất theo yêu cầu Bước 2 `reference/2.plan-trien-khai-project.md`.
2. `Response`: Mô hình bản tin phản hồi giao thức văn bản gửi qua mạng.
3. `StatusCode`: Trạng thái cấp cao của giao thức (ví dụ: `OK`, `ERR`).
4. `ErrorCode`: Mã lỗi chi tiết của hệ thống (ví dụ: `UNKNOWN_COMMAND`, `SYNTAX_ERROR`).

### 2.2. Vấn đề phát sinh
Nếu không làm rõ ranh giới trách nhiệm, hệ thống sẽ gặp các nguy cơ:
- Trùng lặp dữ liệu giữa `KetQua` và `Response`.
- Vi phạm nguyên tắc phân tầng: Tầng nghiệp vụ bị phụ thuộc vào định dạng chuỗi mạng, hoặc tầng mạng phải can thiệp sâu vào cấu trúc dữ liệu nội bộ.

### 2.3. Các phương án giải quyết

- **Phương án 2A (Gộp chung) - [KHÔNG SỬ DỤNG]:** Chỉ dùng `Response` cho toàn bộ hệ thống từ nghiệp vụ đến mạng, bỏ `KetQua`.  
  *Hạn chế:* Không đạt tiêu chí TC3 mức 4 của đề án (yêu cầu một kiểu kết quả dùng chung như `sealed interface KetQua` để kiểm thử nghiệp vụ độc lập).
- **Phương án 2B (Tách biệt ranh giới theo tầng) - [ĐÃ CHỌN]:** Phân định rạch ròi thành 2 phạm vi: **Nghiệp vụ nội bộ (Internal Business)** và **Giao thức truyền thông (Over-the-wire Protocol)**.

### 2.4. Quyết định chính thức và bảng phân định trách nhiệm (Phương án 2B)

> [!IMPORTANT]
> **QUYẾT ĐỊNH 2 - ĐÃ ĐƯỢC NHÓM THỐNG NHẤT:**
> - Dùng `sealed interface KetQua` cho kết quả nghiệp vụ, gồm đúng 2 trường hợp: `KetQua.ThanhCong(String duLieu)` và `KetQua.ThatBai(ErrorCode maLoi, String thongDiep)`.
> - **`CommandDispatcher` trả về `KetQua`**, không trả trực tiếp `Response`.
> - `Response` là `record` của tầng giao thức mạng, chỉ dùng để đóng gói kết quả gửi qua Socket.
> - **`ClientHandler` chịu trách nhiệm chuyển `KetQua` thành `Response`** (tại tầng mạng), sau đó gọi `toProtocolString()` để ghi ra Socket.
> - Tầng nghiệp vụ (`nghiepvu`) **không phụ thuộc** vào `Response`, `StatusCode` hay định dạng chuỗi giao thức mạng.

```
[Tầng Nghiệp vụ / Core Domain]                      [Tầng Mạng / Protocol Layer]
+-------------------------------+                  +-------------------------------+
| KetQua (sealed interface)     |                  | Response (record)             |
|   - ThanhCong(String data)    |  ==Ánh xạ sang==>|   - StatusCode status (OK/ERR)|
|   - ThatBai(ErrorCode, String)|                  |   - String payload            |
+-------------------------------+                  +-------------------------------+
                ^                                                  |
                |                                                  v
     [ErrorCode (enum)]                                 Chuỗi gửi qua Socket:
     - ERR_UNKNOWN_COMMAND                              "OK 14:30:15\n"
     - ERR_SYNTAX                                       "ERR ERR_UNKNOWN_COMMAND ...\n"
     - ERR_INTERNAL ...
```

1. **`ErrorCode` (enum, dùng chung cho `KetQua.ThatBai`, `CommandException` và `Response`):**
   - Danh mục mã định danh lỗi duy nhất: `ERR_UNKNOWN_COMMAND`, `ERR_SYNTAX`, `ERR_INVALID_ARGUMENT`, `ERR_INTERNAL` và mã lỗi Rate Limiter (xem lưu ý bên dưới).
   - Mỗi hằng số enum có thể mang mã chuỗi và mô tả mặc định (áp dụng enum nâng cao Chương 6).
   - **Lưu ý - Mã lỗi Rate Limiter CHƯA THỐNG NHẤT TÊN:** Tài liệu đang dùng song song 2 tên `ERR_RATE_LIMIT` (tại tài liệu này, `ke-hoach-so-do.md` Mục 5 và Mục V.3, Lưu đồ lõi, Sơ đồ lớp UML) và `ERR_RATE_LIMIT_EXCEEDED` (tại `docs/danh-sach-chuc-nang.md` F15 và `ke-hoach-so-do.md` Mục 4). Nhóm cần chọn **một** tên duy nhất trước khi viết mã `ErrorCode`.
2. **`KetQua` (sealed interface, Tầng Nghiệp vụ):**
   - Chỉ dùng bên trong tầng nghiệp vụ, làm kiểu trả về của `CommandDispatcher.dispatch(Request)`.
   - Hoàn toàn độc lập với Socket, IO Stream, giúp việc viết JUnit 5 test cho tầng nghiệp vụ cực kỳ đơn giản và sạch sẽ.
3. **`StatusCode` (enum, Tầng Giao thức Mạng):**
   - Đại diện cho 2 trạng thái cơ bản ở đầu dòng giao thức mạng văn bản: `OK` và `ERR`.
4. **`Response` (record, Tầng Giao thức Mạng):**
   - Được `ClientHandler` tạo ra từ `KetQua` (ví dụ bằng `switch` pattern matching trên `sealed interface`: `ThanhCong` → `Response.ok(...)`, `ThatBai` → `Response.err(...)`).
   - Đóng gói dữ liệu để chuẩn bị serialize ra Socket. Cung cấp phương thức `toProtocolString()`:
     - Nếu thành công: xuất `"OK " + payload + "\n"`
     - Nếu có lỗi: xuất `"ERR " + errorCode.name() + " " + message + "\n"`

---

## 3. VẤN ĐỀ 3: VỊ TRÍ CỦA `MetricsCollector` VÀ `MetricsRepository` TRONG KIẾN TRÚC PHÂN TẦNG
- **Tình trạng:** **Quyết định chính thức - Đã được nhóm thống nhất (Phương án 3B)**.

### 3.1. Hiện trạng (trước khi nhóm thống nhất)
- Trong bản dự thảo đầu tiên của `docs/so-do/ke-hoach-so-do.md`, cả `MetricsCollector` và `MetricsRepository` từng được đặt chung trong nhóm "Dữ liệu & Lưu trữ (package `luutru`)".
- `MetricsCollector` chứa các bộ đếm an toàn luồng (`ConcurrentHashMap`, `AtomicInteger`, `synchronized`), phục vụ lệnh `STATS` qua Streams API.
- `MetricsRepository` chịu trách nhiệm đọc/ghi tệp `metrics.csv` trên đĩa cứng bằng `try-with-resources`.

### 3.2. Vấn đề phát sinh
- `MetricsCollector` thực chất là **trạng thái bộ nhớ dùng chung (In-Memory Shared State)** phục vụ trực tiếp cho nghiệp vụ thống kê thời gian thực của máy chủ. Nó không hề thực hiện bất kỳ thao tác đọc/ghi đĩa cứng nào.
- Nếu đặt `MetricsCollector` ở tầng lưu trữ (`luutru`), tầng nghiệp vụ (`nghiepvu`, cụ thể là lệnh `STATS`) sẽ phải gọi xuống tầng lưu trữ chỉ để lấy dữ liệu RAM. Điều này làm méo mó kiến trúc phân tầng (Tầng nghiệp vụ bị phụ thuộc ngược vào hạ tầng lưu trữ).

### 3.3. Các phương án giải quyết

| Tiêu chí | Phương án 3A: Xếp chung cả hai ở tầng Lưu trữ (`luutru`) **[KHÔNG SỬ DỤNG]** | Phương án 3B: Tách biệt Collector ở Nghiệp vụ, Repository ở Lưu trữ **[ĐÃ CHỌN]** |
|---|---|---|
| **Cấu trúc package** | `luutru.MetricsCollector`, `luutru.MetricsRepository`. | `nghiepvu.MetricsCollector`, `luutru.MetricsRepository`. |
| **Tính độc lập của Nghiệp vụ** | Bị vi phạm: Tầng nghiệp vụ phụ thuộc package lưu trữ để chạy lệnh `STATS`. | **Đạt tuyệt đối (TC3 mức 3):** Tầng nghiệp vụ hoàn toàn tự chủ bộ đếm in-memory. |
| **Nguyên tắc trách nhiệm (SRP)** | Gộp chung trạng thái bộ nhớ với thao tác I/O đĩa. | Tách biệt rành mạch: Collector lo bộ nhớ đa luồng, Repository lo tệp CSV. |
| **Dòng dữ liệu khởi động / tắt** | Nhập nhằng ai gọi ai. | Chuẩn mực Repository pattern: Server khởi động gọi Repository nạp vào Collector; Server tắt gọi Repository lưu Collector. |

### 3.4. Quyết định chính thức của nhóm
> [!IMPORTANT]
> **QUYẾT ĐỊNH 3 - ĐÃ ĐƯỢC NHÓM THỐNG NHẤT: Chọn Phương án 3B:**
> - `MetricsCollector` thuộc package `nghiepvu`: quản lý bộ đếm in-memory an toàn luồng (`AtomicInteger`, `ConcurrentHashMap`) và **tạo `MetricsSnapshot`** (bản sao chụp bất biến) phục vụ `STATS` và lưu trữ; nhận lại `MetricsSnapshot` để khôi phục khi khởi động. `MetricsCollector` không thực hiện I/O tệp.
> - `MetricsRepository` thuộc package `luutru`: chỉ đọc/ghi tệp `metrics.csv` **thông qua `MetricsSnapshot`**, với 2 hàm thuần túy:
>   - `void save(MetricsSnapshot snapshot)`
>   - `MetricsSnapshot load()`
> - `MetricsRepository` **không phụ thuộc trực tiếp** vào `MetricsCollector` hay cấu trúc bộ đếm nội bộ của nó (không nhận tham số `MetricsCollector`, không trả về `MetricsCollector`).
> - `CommandServer` là nơi điều phối: khởi động thì `repository.load()` → `collector.restore(...)`; dừng thì `collector.snapshot()` → `repository.save(...)` (tên phương thức cụ thể chốt khi dựng khung mã, xem mục 5.2).
> 
> Nhờ đó, tầng nghiệp vụ có thể kiểm thử đơn vị độc lập mà không cần tạo tệp giả lập trên ổ đĩa.
>
> *Phương án 3A:* **Không sử dụng**.

---

## 4. VẤN ĐỀ 4: SO SÁNH HAI MÔ HÌNH ĐA LUỒNG (THREAD-PER-CONNECTION vs EXECUTORSERVICE)
- **Tình trạng:** **Quyết định chính thức - Đã được nhóm thống nhất (Mô hình 2: `ExecutorService`)**.

### 4.1. Hiện trạng
- Đề bài yêu cầu: *“Nhiều khách đồng thời; bộ đếm an toàn luồng”*, *“Socket đa luồng”*.
- Trong giáo trình:
  - Chương 5 slide 21–23: Trình bày mô hình `Thread-per-connection` truyền thống (`new Thread(new ClientHandler(socket)).start()`).
  - Chương 4 slide 29–30 và Chương 5 slide 24: Trình bày `ExecutorService` và Bể luồng (`Executors.newFixedThreadPool(...)`).
- Nhóm cần chốt rõ mô hình triển khai để thể hiện đúng trên Sơ đồ Tuần tự, Lưu đồ Lõi và Sơ đồ Lớp UML.

### 4.2. Bảng so sánh chi tiết hai mô hình

| Tiêu chí | Mô hình 1: Thread-per-connection **[KHÔNG SỬ DỤNG]** | Mô hình 2: Thread Pool với `ExecutorService` **[ĐÃ CHỌN]** |
|---|---|---|
| **Cơ chế hoạt động** | Mỗi kết nối mới `accept()` tạo ngay một `new Thread(...)` riêng. Khi client ngắt kết nối, Thread bị hủy. | Khởi tạo sẵn một bể luồng cố định (ví dụ 10-20 workers). Mỗi kết nối mới được đưa vào `threadPool.execute(new ClientHandler(socket))`. |
| **Số worker tối đa xử lý đồng thời** | **Không giới hạn** (bằng số lượng kết nối client hiện tại). | **Cố định `n` luồng** (ví dụ: `newFixedThreadPool(10)` luôn duy trì tối đa đúng 10 worker luồng). |
| **Cơ chế hàng đợi tác vụ chờ** | Không có hàng đợi (kết nối nào vào cũng cấp phát Thread ngay). | Sử dụng hàng đợi **`LinkedBlockingQueue` không giới hạn kích thước** (`Integer.MAX_VALUE`). |
| **Hệ quả trong ứng dụng Socket** | Dễ cạn kiệt bộ nhớ stack (`OutOfMemoryError`) và sập máy chủ nếu có hàng trăm kết nối mở đồng thời. | **Tách biệt rõ:** Tối đa 10 client được phục vụ đồng thời; client thứ 11 trở đi nằm chờ trong `LinkedBlockingQueue` cho đến khi có worker rảnh. |
| **Mức độ xuất hiện trong Slide** | Có ở Chương 5 slide 23 (Code mẫu socket đa luồng). | Có ở Chương 4 slide 29-30 (Bể luồng) và Chương 5 slide 24 (Tối ưu phục vụ client). |
| **Độ phức tạp mã nguồn** | Rất đơn giản, ít dòng code. | Thêm vài dòng cấu hình pool, lệnh `shutdown()` và cần cấu hình timeout. |
| **Đánh giá Rubric chấm** | Đạt mức cơ bản (Tiêu chí 2 mức 3). | **Đạt mức xuất sắc (Tiêu chí 2 mức 5):** Thể hiện sự kết hợp nhuần nhuyễn giữa Chương 4 (Concurrency) và Chương 5 (Networking). |

### 4.3. Phân tích kỹ thuật về hàng đợi và quyết định chính thức
Khi áp dụng `Executors.newFixedThreadPool(10)` cho máy chủ Socket dòng lệnh, cần phân biệt rõ hai khái niệm:
1. **Số worker tối đa đang xử lý đồng thời:** Luôn được giới hạn ở con số cố định là 10.
2. **Số tác vụ chờ trong hàng đợi (`LinkedBlockingQueue`):** Không giới hạn.
3. **Đặc thù phiên làm việc của Socket:**
   - Trong ứng dụng socket TCP, mỗi `ClientHandler` là một tác vụ dạng phiên kéo dài (long-lived connection), chạy vòng lặp `while` liên tục đọc từng dòng lệnh cho đến khi client ngắt kết nối.
   - Do đó, 1 worker thread sẽ bị chiếm dụng toàn bộ thời gian của một phiên client. Khi 10 client đang kết nối, toàn bộ 10 worker đều bận.
   - Nếu client thứ 11 kết nối, lệnh `serverSocket.accept()` vẫn chấp nhận và đưa vào hàng đợi `LinkedBlockingQueue`. Tuy nhiên, **tác vụ của client thứ 11 sẽ phải nằm chờ trong hàng đợi và chưa được đọc hay xử lý bất kỳ lệnh nào** cho đến khi một trong 10 client trước đó đóng socket!
   - Nếu 10 client trước đó treo kết nối không gửi lệnh (idle connection), hàng đợi sẽ bị nghẽn (starvation).

> [!IMPORTANT]
> **QUYẾT ĐỊNH 4 - ĐÃ ĐƯỢC NHÓM THỐNG NHẤT: Chọn Mô hình 2 (`ExecutorService` với Thread Pool cố định) kết hợp Socket Timeout:**
> 1. `CommandServer` khởi tạo và quản lý Thread Pool cố định, ví dụ `ExecutorService threadPool = Executors.newFixedThreadPool(10)` (kích thước cụ thể chốt khi cấu hình).
> 2. Sau khi `accept()`, ngay lập tức thiết lập thời gian chờ đọc:
>    ```java
>    clientSocket.setSoTimeout(60_000); // Tự động ném SocketTimeoutException nếu 60s không gửi lệnh
>    ```
>    Khi xảy ra timeout, luồng worker sẽ chủ động đóng socket nhàn rỗi, giải phóng worker để phục vụ ngay các kết nối đang chờ trong hàng đợi.
> 3. **Trình tự Shutdown chính thức** do `CommandServer.stop()` điều phối:
>    1. Đặt `isRunning = false` và đóng `serverSocket` → **ngừng nhận kết nối mới**.
>    2. Gọi `threadPool.shutdown()` → **yêu cầu các Worker kết thúc** (không nhận thêm tác vụ mới); có thể kết hợp `shutdownNow()` để ngắt các Worker còn treo.
>    3. Gọi `threadPool.awaitTermination(<thời hạn>, <đơn vị>)` → **chờ Worker hoàn tất trong thời hạn phù hợp** (giá trị thời hạn chốt khi dựng khung mã).
>    4. **Chỉ sau đó** mới tạo `MetricsSnapshot` từ `MetricsCollector` và gọi `MetricsRepository.save(snapshot)` để lưu CSV, bảo đảm số liệu không bị Worker cập nhật tiếp sau khi chụp.
> 
> *Chiến lược giải thích khi bảo vệ:*  
> *"Nhóm chọn ExecutorService kết hợp SoTimeout để vừa kiểm soát trần tài nguyên (10 worker), vừa tránh nghẽn hàng đợi LinkedBlockingQueue do các kết nối treo"*.
>
> *Mô hình 1 (`Thread-per-connection`):* **Không sử dụng**.

---

## 5. TỔNG KẾT VÀ BƯỚC TIẾP THEO

### 5.1. Bảng tổng hợp 4 quyết định chính thức

| Vấn đề | Quyết định chính thức | Phương án không sử dụng | Tình trạng | Tác động đến các sơ đồ |
|---|---|---|---|---|
| **1. Exception & Function** | `CommandException extends RuntimeException` mang `ErrorCode` + thông điệp; `CommandDispatcher` bắt và chuyển thành `KetQua.ThatBai`. | 1B (Wrapper interface), 1C (`Function<String, KetQua>`). | **Đã được nhóm thống nhất** | Sơ đồ lớp (phân cấp ngoại lệ), Sơ đồ tuần tự 3.3 (luồng xử lý lỗi). |
| **2. Phân biệt kết quả** | `CommandDispatcher.dispatch(Request)` trả `KetQua` (`ThanhCong` / `ThatBai`); `ClientHandler` chuyển `KetQua` → `Response`; `Response` chỉ thuộc tầng giao thức mạng. | 2A (chỉ dùng `Response`). | **Đã được nhóm thống nhất** | Sơ đồ lớp, Sơ đồ kiến trúc tầng, Sơ đồ tuần tự 3.1, 3.2, 3.3. |
| **3. Vị trí Metrics** | `nghiepvu.MetricsCollector` (bộ đếm in-memory + tạo `MetricsSnapshot`); `luutru.MetricsRepository` (đọc/ghi CSV qua `MetricsSnapshot`). | 3A (gộp cả hai vào `luutru`). | **Đã được nhóm thống nhất** | Sơ đồ kiến trúc tầng, Sơ đồ lớp, Sơ đồ tuần tự 3.2, 3.4. |
| **4. Mô hình đa luồng** | `ExecutorService` Thread Pool cố định + `Socket.setSoTimeout()`; `CommandServer` quản lý vòng đời pool; Shutdown: ngừng nhận kết nối → `shutdown()` → `awaitTermination(...)` → snapshot → lưu CSV. | Thread-per-connection. | **Đã được nhóm thống nhất** | Lưu đồ lõi, Sơ đồ lớp, Sơ đồ tuần tự 3.4. |

### 5.2. Các điểm chi tiết CHƯA được 4 quyết định trên chốt (cần nhóm thống nhất khi dựng khung mã)

> [!WARNING]
> Các mục dưới đây là **mâu thuẫn tên gọi / chữ ký** phát hiện khi rà soát chéo tài liệu và sơ đồ. Chúng **không thuộc phạm vi 4 quyết định đã chốt**, nên tài liệu này chỉ ghi nhận, **không tự quyết định thay nhóm**.

| # | Nội dung | Các biến thể đang tồn tại | Vị trí |
|---|---|---|---|
| 1 | **Tên mã lỗi Rate Limiter** | `ERR_RATE_LIMIT` ↔ `ERR_RATE_LIMIT_EXCEEDED` | `ERR_RATE_LIMIT`: tài liệu này (2.4), `ke-hoach-so-do.md` Mục 5 & V.3, Lưu đồ lõi, Sơ đồ lớp UML. `ERR_RATE_LIMIT_EXCEEDED`: `danh-sach-chuc-nang.md` (F15), `ke-hoach-so-do.md` Mục 4. |
| 2 | **Tên ngoại lệ lỗi cú pháp** | `CommandSyntaxException` ↔ `CommandException(ERR_SYNTAX, ...)` | Đã thống nhất dùng `CommandException(ERR_SYNTAX, ...)` theo Quyết định 1; đã đồng bộ trên Sơ đồ tuần tự 3.3 và tài liệu kế hoạch. |
| 3 | **Tên phương thức snapshot của `MetricsCollector`** | `getSnapshot()` ↔ `exportSnapshot()`; khôi phục: `restoreFromSnapshot(...)` | Sơ đồ tuần tự 3.2 (bước 6), Sơ đồ tuần tự 3.4 (bước 2), Sơ đồ lớp UML. |
| 4 | **Chữ ký `MetricsRepository`** | `save(MetricsSnapshot)` / `MetricsSnapshot load()` ↔ `saveToCsv` / `loadFromCsv()` ↔ `load(): Optional<MetricsSnapshot>` | Tài liệu này (3.4), Sơ đồ tuần tự 3.4 (bước 2, 5), Sơ đồ lớp UML. |
| 5 | **Thành phần của `record Response`** | `(status, data, errorMessage)` ↔ `(status, payload)` ↔ `(status, payload, errorCode)` | `ke-hoach-so-do.md` Mục 5, Sơ đồ tuần tự 3.3 (lifeline `Response`), Sơ đồ lớp UML. |
| 6 | **Tên thành phần tham số của `Request`** | `arguments()` ↔ `args()` | `ke-hoach-so-do.md` Mục 5, tài liệu này (1.4) ↔ Sơ đồ tuần tự 3.3, Lưu đồ lõi. |
| 7 | **Package đặt `ErrorCode` / `CommandException`** | Bản phân tích 2.4 ban đầu ghi "Tầng Nghiệp vụ" ↔ Sơ đồ lớp UML đặt trong `giaothuc` (khối Shared Models) | Tài liệu này (2.4 cũ), Sơ đồ lớp UML. |

### 5.3. Bước tiếp theo
Toàn bộ tài liệu `docs/so-do/ke-hoach-so-do.md`, `docs/so-do/ra-soat-thiet-ke.md` và các sơ đồ `.drawio` trong `docs/so-do/` đã được rà soát và đồng bộ thống nhất theo 4 quyết định thiết kế chính thức. Các nhãn đề xuất/chưa chốt trên các thành phần cốt lõi đã được loại bỏ, kiểu dáng viền đứt đã được chuyển thành nét liền chính thức, và luồng chuyển đổi giữa `KetQua` và `Response` tại `ClientHandler` đã được thể hiện đồng nhất xuyên suốt. Nhóm sẵn sàng chuyển sang Bước 3 (dựng khung mã nguồn Java).
