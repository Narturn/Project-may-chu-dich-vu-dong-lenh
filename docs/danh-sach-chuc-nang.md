# BẢNG DANH SÁCH CHỨC NĂNG VÀ KẾ HOẠCH TRIỂN KHAI
## ĐỀ TÀI 31: MÁY CHỦ DỊCH VỤ DÒNG LỆNH
**Học phần:** Lập trình Java nâng cao (AJP201)  

---

## I. NGUYÊN TẮC PHÂN LOẠI NGUỒN CHỨC NĂNG

Hệ thống chức năng được phân loại thành 3 nhóm độc lập theo đúng quy định:
1. **Phần lõi (Core):** Là hạ tầng và cơ chế vận hành thiết yếu nhất để hình thành nên một máy chủ dịch vụ dòng lệnh (Command-line Service Server). Nếu thiếu các thành phần này, hệ thống hoàn toàn không thể khởi động, không thể tiếp nhận kết nối mạng hay điều phối xử lý bất kỳ lệnh nào.
2. **Phải làm (Mandatory):** Là các yêu cầu nghiệp vụ và kỹ thuật được đề bài Đề số 31 và Quy chế chung (Mục 1.2 `Main.pdf`) yêu cầu trực tiếp bắt buộc phải hoàn thành (các lệnh `TIME`, `DATE`, `ECHO`, `UPPER`, `STATS`; bộ đếm an toàn luồng; trả mã lỗi và thông báo; lưu trữ tệp; ứng dụng client).
3. **Mở rộng (Extension):** Là các chức năng bổ sung nâng cao được nêu trong phần "Mở rộng" của Đề 31 (`CALC`, `HISTORY`, giới hạn tốc độ theo cửa sổ thời gian) và tiện ích trợ giúp điều khiển phiên (`HELP`, `QUIT`), chỉ triển khai sau khi đã hoàn thành trọn vẹn và vượt qua kiểm thử phần lõi và phần phải làm.

---

## II. BẢNG DANH SÁCH CHỨC NĂNG CHI TIẾT

| Mã | Chức năng | Nguồn (Phần lõi / Phải làm / Mở rộng) | Trường hợp biên | Kỹ thuật Java (chương) | Phụ thuộc | Người phụ trách |
|---|---|---|---|---|---|---|
| **F01** | **Mô hình hóa bản tin giao thức và phản hồi (Request / Response Model)**<br>Định nghĩa cấu trúc dữ liệu bản tin yêu cầu (Request: tên lệnh, chuỗi tham số) và bản tin phản hồi (Response: mã trạng thái, dữ liệu/thông điệp, nhãn thời gian) dưới dạng các đối tượng dữ liệu bất biến. | **Phần lõi**<br>*(Nền tảng biểu diễn dữ liệu trao đổi trong toàn bộ hệ thống)* | - Tham số chứa ký tự điều khiển (`\r`, `\n`) gây vỡ dòng giao thức.<br>- Tên lệnh hoặc dữ liệu phản hồi bị null/rỗng.<br>- Dữ liệu phản hồi chứa ký tự xuống dòng nhiều tầng. | `record (Ch6)`, `enum (Ch6)` | Không | Chưa phân công |
| **F02** | **Hệ thống mã trạng thái và ngoại lệ giao thức (Protocol Error Codes & Custom Exceptions)**<br>Định nghĩa enum các mã trạng thái/mã lỗi chuẩn (`OK`, `ERR_UNKNOWN_COMMAND`, `ERR_SYNTAX`, `ERR_INTERNAL`, `ERR_RATE_LIMIT`...) và hệ thống ngoại lệ riêng cho tầng giao thức dịch vụ dòng lệnh. | **Phần lõi**<br>*(Cơ chế xử lý lỗi và đồng nhất mã kết quả cho toàn bộ luồng xử lý)* | - Ngoại lệ bị nuốt (swallowed) không để lại log/dấu vết.<br>- Ngoại lệ không kiểm soát làm sập luồng phục vụ client.<br>- Mã trạng thái bị trùng lặp hoặc không ánh xạ được sang thông điệp lỗi. | `custom exception (Ch6)`, `enum (Ch6)` | Không | Chưa phân công |
| **F03** | **Phân tích cú pháp dòng lệnh và chuẩn hóa giao thức văn bản (Text Protocol Parser)**<br>Đọc dòng văn bản thô từ luồng socket, cắt bỏ khoảng trắng thừa đầu/cuối, tách tên lệnh (không phân biệt chữ hoa/thường) và phần tham số còn lại, chuyển đổi thành đối tượng Request chuẩn. | **Phần lõi**<br>*(Cửa ngõ phân tích cú pháp dữ liệu đầu vào của giao thức)* | - Dòng rỗng hoặc chỉ toàn dấu cách/tab.<br>- Nhiều khoảng trắng liên tiếp giữa tên lệnh và tham số.<br>- Dòng lệnh vượt quá độ dài tối đa cho phép (Buffer overflow do chuỗi quá lớn).<br>- Ký tự kết thúc dòng không đồng nhất (`\r\n` của Windows vs `\n` của Linux). | `switch expression (Ch6)`, `Optional (Ch6)`, `custom exception (Ch6)` | F01, F02 | Chưa phân công |
| **F04** | **Khung đăng ký và điều phối lệnh (Command Registry & Dispatcher)**<br>Quản lý tập hợp các bộ xử lý lệnh thông qua cấu trúc ánh xạ `Map<String, Function<String, String>>`. Cho phép đăng ký các bộ xử lý lệnh linh hoạt bằng Lambda/Method reference và tra cứu bộ xử lý theo tên lệnh. | **Phần lõi**<br>*(Cấu trúc cốt lõi định hình kiến trúc máy chủ dịch vụ dòng lệnh theo đề bài)* | - Đăng ký trùng lặp tên lệnh (cần cơ chế ghi đè hoặc từ chối).<br>- Tên lệnh gọi tới không tồn tại trong Registry.<br>- Bộ xử lý (Function) trả về chuỗi null hoặc ném RuntimeException khi đang thực thi. | `Map (Ch3)`, `HashMap (Ch3)`, `Function (Ch2)`, `Lambda (Ch2)`, `Optional (Ch6)` | F01, F02 | Chưa phân công |
| **F05** | **Máy chủ mạng TCP và quản lý kết nối đa luồng (Multi-threaded TCP Server)**<br>Khởi tạo `ServerSocket` lắng nghe cổng xác định, vòng lặp tiếp nhận kết nối (`accept()`) và cấp phát luồng xử lý riêng cho từng client (Thread-per-connection / Thread Pool) để đọc/ghi dữ liệu hai chiều. Quản lý tài nguyên an toàn bằng `try-with-resources`. | **Phần lõi**<br>*(Hạ tầng mạng đa luồng bắt buộc để phục vụ nhiều máy khách đồng thời)* | - Cổng dịch vụ đã bị chiếm dụng (`BindException`).<br>- Client ngắt kết nối đột ngột (RST/đóng cửa sổ) khi máy chủ đang đọc/ghi (`SocketException`, `EOFException`).<br>- Máy chủ nhận lệnh dừng đột ngột (shutdown hook) khi đang có kết nối hoạt động.<br>- Cạn kiệt luồng nếu số lượng kết nối tăng vọt. | `ServerSocket (Ch5)`, `Socket (Ch5)`, `BufferedReader (Ch5, Ch6)`, `PrintWriter (Ch5)`, `Thread (Ch4)`, `Runnable (Ch4)`, `ExecutorService (Ch4)`, `try-with-resources (Ch5, Ch6)` | F03, F04 | Chưa phân công |
| **F06** | **Bộ xử lý lệnh tiện ích `TIME` và `DATE` (Date & Time Commands)**<br>Triển khai đăng ký bộ xử lý cho lệnh `TIME` (trả về thời gian hiện tại: giờ, phút, giây) và `DATE` (trả về ngày hiện tại: năm, tháng, ngày) sử dụng gói thời gian chuẩn `java.time`. | **Phải làm**<br>*(Chức năng được đề bài yêu cầu trực tiếp trong mục Ứng dụng mạng)* | - Client truyền thêm tham số rác phía sau lệnh `TIME` / `DATE` (cần quy ước bỏ qua tham số hoặc báo lỗi cú pháp).<br>- Thời gian hệ thống thay đổi/nhảy lùi (NTP sync/Daylight saving).<br>- Nhiều client yêu cầu đồng thời trong cùng một mili-giây. | `java.time (Ch5)`, `Function (Ch2)`, `Lambda (Ch2)` | F04 | Chưa phân công |
| **F07** | **Bộ xử lý lệnh tiện ích `ECHO` và `UPPER` (Echo & Upper String Commands)**<br>Triển khai đăng ký bộ xử lý cho lệnh `ECHO` (phản hồi nguyên vẹn chuỗi tham số) và `UPPER` (chuyển toàn bộ chuỗi tham số thành ký tự in hoa). | **Phải làm**<br>*(Chức năng được đề bài yêu cầu trực tiếp trong mục Ứng dụng mạng)* | - Không có tham số truyền vào (chuỗi rỗng).<br>- Tham số chứa ký tự tiếng Việt có dấu Unicode (`đ`, `ê`, `ư`...).<br>- Tham số chỉ chứa khoảng trắng hoặc ký tự đặc biệt tab/dấu nháy.<br>- Chuỗi tham số có kích thước cực lớn. | `Function (Ch2)`, `Lambda (Ch2)` | F04 | Chưa phân công |
| **F08** | **Quản lý bộ đếm thống kê an toàn luồng (Thread-safe Metrics Collector)**<br>Quản lý các bộ đếm dùng chung giữa nhiều luồng: tổng số kết nối đã tiếp nhận, số kết nối đang hoạt động, và số lần thực thi của từng loại lệnh. Đảm bảo an toàn luồng tuyệt đối khi nhiều luồng worker cập nhật đồng thời. | **Phải làm**<br>*(Yêu cầu trực tiếp trong mục Đồng thời: "Nhiều khách đồng thời; bộ đếm an toàn luồng")* | - Tranh chấp cập nhật đồng thời (race condition / lost update) khi nhiều client gọi lệnh cùng lúc.<br>- Client ngắt kết nối bất thường dẫn đến sai lệch số kết nối đang hoạt động.<br>- Tràn số nguyên nếu hệ thống chạy liên tục trong thời gian dài (overflow). | `ConcurrentHashMap (Ch5)`, `synchronized (Ch4)`<br>*(Lưu ý: Đề bài yêu cầu AtomicInteger; trong slide Ch4 giảng về atomicity và java.util.concurrent nhưng không có tên lớp AtomicInteger)* | F05 | Chưa phân công |
| **F09** | **Bộ xử lý lệnh tiện ích `STATS` với Streams API (Statistics Command with Streams)**<br>Triển khai đăng ký bộ xử lý cho lệnh `STATS`: sử dụng Java Streams API để tổng hợp, tính toán số liệu từ bộ đếm (tổng kết nối, phân loại số lượt thực thi theo loại lệnh, sắp xếp lệnh theo tần suất sử dụng) và xuất kết quả định dạng rõ ràng. | **Phải làm**<br>*(Yêu cầu trực tiếp trong mục Dữ liệu và Streams: "STATS đếm kết nối và lệnh theo loại bằng Streams")* | - Máy chủ vừa khởi động chưa có kết nối hoặc lệnh nào được thực thi (tập dữ liệu rỗng).<br>- Đọc dữ liệu thống kê bằng Stream trong khi các luồng khác đang ghi (`ConcurrentModificationException` nếu không dùng cấu trúc phù hợp).<br>- Danh sách loại lệnh có tần suất bằng 0. | `Streams (Ch2)`, `Function (Ch2)`, `Lambda (Ch2)`, `Map (Ch3)`, `Comparator (Ch3)` | F04, F08 | Chưa phân công |
| **F10** | **Chuẩn hóa phản hồi trạng thái và thông báo lỗi (Standardized Status & Error Response)**<br>Xây dựng tầng đóng gói phản hồi chuẩn theo giao thức văn bản: lệnh thành công trả `OK <kết_quả>`, lệnh lỗi trả `ERR <mã_lỗi> <thông_báo>` đúng yêu cầu "lỗi trả mã và thông báo" của đề bài. | **Phải làm**<br>*(Yêu cầu trực tiếp trong mục Ứng dụng mạng: "lỗi trả mã và thông báo")* | - Thông báo lỗi chứa ký tự xuống dòng (`\n`, `\r`) làm gãy dòng giao thức của client.<br>- Lỗi ngoại lệ nội bộ không mong muốn (Unchecked Exception/Error) phải được chuyển thành mã lỗi chuẩn `ERR_INTERNAL` thay vì làm sập kết nối.<br>- Mã lỗi không nằm trong danh mục đã định nghĩa. | `enum (Ch6)`, `record (Ch6)`, `switch expression (Ch6)`, `custom exception (Ch6)` | F01, F02, F03 | Chưa phân công |
| **F11** | **Ứng dụng máy khách dòng lệnh mẫu (Interactive Console Client)**<br>Xây dựng ứng dụng Client độc lập kết nối đến Server qua Socket, cho phép người dùng nhập lệnh từ bàn phím, gửi lên server, nhận và hiển thị phản hồi có định dạng, đồng thời hỗ trợ thoát an toàn. | **Phải làm**<br>*(Yêu cầu bắt buộc để tạo thành hệ thống mạng Client-Server hoàn chỉnh chạy được theo Chuẩn đầu ra và Rubric chấm)* | - Kết nối thất bại do Server chưa bật hoặc sai địa chỉ IP/cổng (`ConnectException`).<br>- Mất kết nối đột ngột giữa chừng khi đang chờ Server trả lời.<br>- Người dùng nhập ký tự ngắt (Ctrl+C, EOF) từ bàn phím.<br>- Xung đột mã hóa ký tự (UTF-8) khi in tiếng Việt ra console. | `Socket (Ch5)`, `BufferedReader (Ch5, Ch6)`, `PrintWriter (Ch5)`, `try-with-resources (Ch5, Ch6)` | F05, F10 | Chưa phân công |
| **F12** | **Lưu trữ và khôi phục số liệu thống kê ra tệp văn bản / CSV (Metrics Persistence)**<br>Ghi nhận số liệu thống kê lệnh và kết nối định kỳ hoặc khi tắt server ra tệp văn bản/CSV; tự động nạp lại số liệu khi khởi động máy chủ để bảo toàn dữ liệu. | **Phải làm**<br>*(Yêu cầu bắt buộc chung cho mọi đề tại Mục 1.2 Main.pdf: "Dữ liệu lưu bằng tệp văn bản, CSV hoặc tuần tự hóa")* | - Tệp dữ liệu chưa tồn tại trong lần chạy đầu tiên (cần tự động tạo mới).<br>- Tệp bị lỗi định dạng, bị sửa đổi trái phép hoặc rỗng.<br>- Không có quyền đọc/ghi tệp trên hệ điều hành (`AccessDeniedException`).<br>- Máy chủ bị tắt cưỡng bức (kill -9) không kịp kích hoạt hook lưu tệp. | `try-with-resources (Ch5, Ch6)`, `BufferedReader (Ch5, Ch6)`, `custom exception (Ch6)` | F08 | Chưa phân công |
| **F13** | **Bộ xử lý lệnh tiện ích `CALC` đánh giá biểu thức (Math Expression Evaluator)**<br>Triển khai đăng ký bộ xử lý cho lệnh `CALC <biểu_thức>`: phân tích từ vựng và đánh giá biểu thức toán học chứa số, các phép toán `+`, `-`, `*`, `/` và dấu ngoặc `()` bằng thuật toán ký pháp Ba Lan (Shunting-yard) hoặc cây cú pháp thuần Java. | **Mở rộng**<br>*(Tính năng mở rộng nêu trong đề bài: "Lệnh CALC biểu thức")* | - Biểu thức rỗng hoặc chỉ có khoảng trắng.<br>- Lỗi chia cho 0 (`Divide by zero`).<br>- Sai cú pháp dấu ngoặc: ngoặc mở không đóng, ngoặc đóng thừa.<br>- Hai toán tử đứng liền nhau (`1 + * 2`), biểu thức chứa ký tự chữ cái không hợp lệ.<br>- Tràn số nguyên/số thực khi tính toán. | `Queue (Ch3)`, `Deque (Ch3)`, `ArrayDeque (Ch3)`, `switch expression (Ch6)`, `custom exception (Ch6)`, `Function (Ch2)`, `Lambda (Ch2)` | F04, F10 | Chưa phân công |
| **F14** | **Quản lý `HISTORY` theo từng kết nối client (Session Command History)**<br>Lưu vết danh sách các lệnh kèm thời điểm thực thi của từng kết nối khách riêng biệt; cung cấp lệnh `HISTORY` để client tra cứu lại các lệnh mình đã gửi trong phiên làm việc hiện tại. | **Mở rộng**<br>*(Tính năng mở rộng nêu trong đề bài: "HISTORY từng kết nối")* | - Client vừa kết nối, chưa gửi lệnh nào mà gọi `HISTORY` (cần trả về danh sách rỗng thay vì lỗi).<br>- Client gửi quá nhiều lệnh trong một phiên gây tràn bộ nhớ (cần giới hạn N lệnh gần nhất).<br>- Bản thân lệnh `HISTORY` có được lưu vào lịch sử hay không (cần quy ước thống nhất). | `List (Ch3)`, `ArrayList (Ch3)`, `Deque (Ch3)`, `record (Ch6)`, `Streams (Ch2)`, `java.time (Ch5)` | F04, F05, F10 | Chưa phân công |
| **F15** | **Giới hạn tốc độ theo cửa sổ thời gian (Rate Limiter per Time Window)**<br>Giới hạn số lượng yêu cầu tối đa mà một kết nối/IP được phép gửi trong một cửa sổ thời gian (ví dụ: tối đa N yêu cầu trong W giây). Nếu vượt quá ngưỡng, từ chối xử lý và trả về mã lỗi `ERR_RATE_LIMIT_EXCEEDED`. | **Mở rộng**<br>*(Tính năng mở rộng nêu trong đề bài: "giới hạn tốc độ theo cửa sổ thời gian")* | - Client gửi lượng yêu cầu dồn dập trong cùng 1 mili-giây (burst traffic).<br>- Trượt cửa sổ thời gian và dọn dẹp các mốc thời gian cũ để tránh rò rỉ bộ nhớ.<br>- Tranh chấp đồng thời khi nhiều luồng kiểm tra và ghi nhận lượt truy cập của cùng một địa chỉ.<br>- Rò rỉ bộ nhớ do lưu thông tin client đã ngắt kết nối từ lâu. | `Queue (Ch3)`, `ArrayDeque (Ch3)`, `ConcurrentHashMap (Ch5)`, `synchronized (Ch4)`, `java.time (Ch5)` | F05, F10 | Chưa phân công |
| **F16** | **Bộ xử lý lệnh điều khiển phiên `HELP` và `QUIT` / `EXIT` (Session Control Commands)**<br>Cung cấp lệnh `HELP` để liệt kê danh sách lệnh hỗ trợ cùng cú pháp tóm tắt, và lệnh `QUIT` / `EXIT` để client chủ động ngắt kết nối an toàn với máy chủ sau khi nhận phản hồi chào tạm biệt. | **Mở rộng**<br>*(Tiện ích mở rộng hoàn thiện giao thức dòng lệnh)* | - Client gửi `QUIT` kèm tham số thừa.<br>- Máy chủ đóng socket trước khi luồng dữ liệu phản hồi kịp truyền hết sang client (cần flush trước khi close).<br>- Gọi `HELP` khi hệ thống có thêm các lệnh mở rộng động. | `Map (Ch3)`, `Function (Ch2)`, `Lambda (Ch2)`, `Streams (Ch2)` | F04, F05, F10 | Chưa phân công |

---

## III. KẾ HOẠCH TRIỂN KHAI CHI TIẾT

Kế hoạch triển khai được xây dựng dựa trên danh sách 16 chức năng trên (`F01` đến `F16`), phân bổ theo 4 giai đoạn logic từ nền tảng đến nâng cao nhằm đáp ứng các mốc đánh giá trong tiến độ 12 tuần của môn học (Đặc biệt mốc Tuần 5: Bản thiết kế và Tuần 8: Demo chạy được dữ liệu & chức năng lõi).

### 1. Các giai đoạn phát triển

```
+-----------------------------------------------------------------------------------+
| Giai đoạn 1: Nền tảng Giao thức & Kiến trúc Xử lý Lệnh (Core Foundation)          |
| -> F01, F02, F03, F04                                                             |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| Giai đoạn 2: Hạ tầng Mạng TCP Đa luồng & Lệnh Tiện ích Cơ bản (Networking & Cmds) |
| -> F05, F06, F07, F10, F11                                                        |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| Giai đoạn 3: Thống kê An toàn luồng, Streams & Lưu trữ Tệp (Metrics & Storage)    |
| -> F08, F09, F12                                                                  |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| Giai đoạn 4: Tính năng Mở rộng Nâng cao & Hoàn thiện Hệ thống (Advanced Features)  |
| -> F13, F14, F15, F16                                                             |
+-----------------------------------------------------------------------------------+
```

---

### 2. Thứ tự thực hiện các chức năng trong từng giai đoạn

#### Giai đoạn 1: Nền tảng Giao thức & Kiến trúc Xử lý Lệnh (F01, F02, F03, F04)
1. **F01 (Mô hình Request/Response):** Xây dựng các record mang dữ liệu bất biến và enum mã trạng thái cơ bản.
2. **F02 (Mã lỗi & Ngoại lệ):** Xây dựng cây ngoại lệ riêng cho tầng giao thức dịch vụ dòng lệnh.
3. **F03 (Phân tích cú pháp văn bản):** Xây dựng parser chuẩn hóa chuỗi đầu vào, phân tích dòng lệnh thành Request.
4. **F04 (Khung đăng ký Command Registry):** Xây dựng registry lưu trữ bộ xử lý bằng `Map<String, Function<String, String>>` và cơ chế điều phối.

#### Giai đoạn 2: Hạ tầng Mạng TCP Đa luồng & Lệnh Tiện ích Cơ bản (F05, F06, F07, F10, F11)
1. **F10 (Chuẩn hóa phản hồi trạng thái & lỗi):** Hoàn thiện tầng định dạng phản hồi chuẩn (`OK ...`, `ERR ...`).
2. **F06 (Lệnh TIME, DATE) & F07 (Lệnh ECHO, UPPER):** Triển khai 4 bộ xử lý lệnh cơ bản đầu tiên và đăng ký vào Command Registry (F04).
3. **F05 (Máy chủ mạng TCP đa luồng):** Xây dựng ServerSocket, vòng lặp accept và worker xử lý đa luồng cho từng client kết nối, tích hợp parser (F03) và registry (F04).
4. **F11 (Ứng dụng Console Client):** Xây dựng client kết nối socket kiểm thử trực tiếp giao tiếp hai chiều với server.

#### Giai đoạn 3: Thống kê An toàn luồng, Streams & Lưu trữ Tệp (F08, F09, F12)
1. **F08 (Bộ đếm thống kê an toàn luồng):** Tích hợp bộ đếm an toàn luồng (kết nối, tần suất từng loại lệnh) vào luồng phục vụ của máy chủ.
2. **F09 (Lệnh STATS với Streams API):** Triển khai bộ xử lý lệnh `STATS`, áp dụng Streams API để tổng hợp, sắp xếp và trình bày số liệu thống kê.
3. **F12 (Lưu trữ và nạp số liệu ra tệp CSV):** Xây dựng cơ chế lưu trữ bền vững số liệu thống kê ra tệp văn bản/CSV khi server đóng và nạp lại khi khởi động.

#### Giai đoạn 4: Tính năng Mở rộng Nâng cao & Hoàn thiện Hệ thống (F13, F14, F15, F16)
1. **F16 (Lệnh HELP & QUIT):** Bổ sung tiện ích tra cứu trợ giúp và lệnh thoát ngắt kết nối an toàn.
2. **F13 (Lệnh CALC đánh giá biểu thức):** Triển khai thuật toán phân tích cú pháp biểu thức toán học và đánh giá kết quả.
3. **F14 (Lịch sử lệnh HISTORY từng kết nối):** Xây dựng hàng đợi lưu vết lịch sử lệnh trong phiên làm việc của từng client.
4. **F15 (Giới hạn tốc độ Rate Limiter theo cửa sổ thời gian):** Xây dựng bộ lọc rate limiting trượt thời gian bảo vệ server chống spam yêu cầu.

---

### 3. Những chức năng có thể triển khai song song

Khi làm việc nhóm 2 người, các cặp chức năng sau có tính độc lập cao và có thể giao song song:
- **Trong Giai đoạn 1:**
  - Thành viên A: Xây dựng **F01** (Record mô hình dữ liệu) và **F02** (Ngoại lệ & mã lỗi).
  - Thành viên B: Chuẩn bị đặc tả định dạng dòng lệnh và bộ khung parser **F03**.
- **Trong Giai đoạn 2:**
  - Thành viên A: Triển khai các bộ xử lý lệnh **F06** (`TIME`, `DATE`) và **F07** (`ECHO`, `UPPER`).
  - Thành viên B: Triển khai hạ tầng mạng TCP đa luồng **F05** và **F11** (Console Client).
- **Trong Giai đoạn 3:**
  - Thành viên A: Xây dựng bộ đếm an toàn luồng **F08** và lệnh **F09** (`STATS` với Streams API).
  - Thành viên B: Xây dựng module đọc/ghi tệp bền vững **F12** (Persistence CSV).
- **Trong Giai đoạn 4:**
  - Thành viên A: Triển khai **F13** (`CALC` biểu thức toán học) và **F16** (`HELP`, `QUIT`).
  - Thành viên B: Triển khai **F14** (`HISTORY` phiên) và **F15** (Rate Limiter).

---

### 4. Các mối phụ thuộc cần hoàn thành trước (Critical Path)

Chuỗi phụ thuộc trọng yếu quyết định tiến độ toàn dự án:
1. **F01 & F02** là nền móng bắt buộc: Toàn bộ các module sau đều phụ thuộc vào mô hình bản tin và hệ thống lỗi.
2. **F03 & F04** là khung nghiệp vụ trung tâm: Mọi lệnh (`TIME`, `DATE`, `ECHO`, `UPPER`, `STATS`, `CALC`, `HISTORY`) đều là các `Function<String, String>` được đăng ký vào `Map` của F04 thông qua đầu vào từ F03.
3. **F05** là cầu nối mạng: Không có F05 thì các lệnh chỉ chạy được dưới dạng kiểm thử đơn vị nội bộ, chưa thể kiểm thử client-server.
4. **F08** phải hoàn thành trước **F09** và **F12**: Lệnh `STATS` và module lưu tệp đều lấy nguồn dữ liệu từ bộ đếm an toàn luồng F08.

---

### 5. Kết quả cần đạt sau mỗi giai đoạn (Definition of Done)

- **Sau Giai đoạn 1 (Tuần 5 - Bản thiết kế hoàn chỉnh):**
  - Đóng gói trọn vẹn tầng nghiệp vụ lõi độc lập với mạng.
  - Kiểm thử đơn vị (Unit Test) cho Parser (F03) và Registry (F04) đạt 100% ca kiểm thử với các lệnh giả lập (Mock Functions).
- **Sau Giai đoạn 2 (Tuần 8 - Demo tại lớp chạy được phần lõi & dữ liệu):**
  - Máy chủ TCP phục vụ đồng thời ít nhất 5-10 client kết nối cùng lúc mà không bị treo hay nghẽn luồng.
  - Client gửi lệnh `TIME`, `DATE`, `ECHO`, `UPPER` nhận được kết quả chính xác có gắn mã `OK` hoặc `ERR`.
  - Không rò rỉ tài nguyên socket nhờ `try-with-resources`.
- **Sau Giai đoạn 3 (Tuần 10 - Hoàn thiện toàn bộ chức năng bắt buộc):**
  - Lệnh `STATS` xuất dữ liệu thống kê tổng hợp chính xác bằng Streams API ngay cả khi có tải truy cập cao.
  - Dữ liệu thống kê được bảo toàn qua các lần khởi động lại máy chủ nhờ tệp CSV/văn bản.
  - Bộ đếm không bị race condition dưới kiểm thử đa luồng dồn dập.
- **Sau Giai đoạn 4 (Tuần 11-12 - Đóng gói nghiệm thu & Bảo vệ):**
  - Chạy mượt mà toàn bộ các tính năng mở rộng: `CALC` tính đúng biểu thức phức tạp, `HISTORY` ghi vết chính xác theo từng client, Rate Limiter chặn đúng ngưỡng giới hạn tốc độ.
  - Đóng gói dự án thành tệp JAR thực thi độc lập (`java -jar`).
  - Toàn bộ test case tự động đạt (PASS), sẵn sàng kịch bản demo bảo vệ trước giảng viên.

---

### 6. Những rủi ro hoặc vấn đề kỹ thuật cần lưu ý

1. **Tranh chấp dữ liệu (Race Condition) trên Bộ đếm thống kê:**
   - *Nguy cơ:* Khi nhiều luồng client đồng thời gọi lệnh, thao tác tăng bộ đếm trên `int` thông thường (`count++`) sẽ gây mất mát số liệu (lost update).
   - *Biện pháp:* Sử dụng cấu trúc đồng bộ hóa `synchronized` (Ch4) hoặc `ConcurrentHashMap` (Ch5) để đảm bảo tính nguyên tử (atomicity). Đối với kỹ thuật `AtomicInteger` yêu cầu từ đề bài, cần lưu ý áp dụng đúng chuẩn `java.util.concurrent`.
2. **Nghẽn luồng và Cạn kiệt tài nguyên (Thread Starvation / Out of Memory):**
   - *Nguy cơ:* Nếu tạo một `Thread` mới vô hạn cho mỗi kết nối mà client không ngắt hoặc treo kết nối, server sẽ cạn kiệt bộ nhớ và CPU.
   - *Biện pháp:* Sử dụng `ExecutorService` (Ch4) với Thread Pool có giới hạn (Fixed/Cached Thread Pool) kết hợp đặt thời gian chờ (`Socket.setSoTimeout`) để tự động đóng các kết nối nhàn rỗi (idle).
3. **Vỡ định dạng giao thức dòng (Protocol Stream Framing Issue):**
   - *Nguy cơ:* Tham số hoặc dữ liệu trả về chứa ký tự xuống dòng (`\r\n` hoặc `\n`) khiến `BufferedReader.readLine()` ở phía bên kia tưởng nhầm là bản tin mới, dẫn đến lệch pha đồng bộ bản tin.
   - *Biện pháp:* Chuẩn hóa dữ liệu truyền tải trên giao thức văn bản một dòng (single-line protocol), thay thế hoặc escape các ký tự xuống dòng nội dung trước khi gửi qua mạng.
4. **Xung đột đọc/ghi đồng thời trong Streams API:**
   - *Nguy cơ:* Khi lệnh `STATS` đang duyệt Stream trên `Map` thống kê thì một luồng client khác lại ghi thêm dữ liệu, dễ dẫn đến ngoại lệ `ConcurrentModificationException`.
   - *Biện pháp:* Sử dụng `ConcurrentHashMap` hoặc tạo một bản sao chụp cục bộ (snapshot/copy) an toàn luồng trước khi mở luồng Stream tổng hợp.
5. **Rò rỉ bộ nhớ ở các tính năng Mở rộng (Rate Limiter & History):**
   - *Nguy cơ:* Quản lý lịch sử và cửa sổ thời gian cho từng kết nối mà không dọn dẹp khi client ngắt kết nối sẽ tích lũy dữ liệu thừa trong RAM.
   - *Biện pháp:* Gắn vòng đời của `History` và `RateLimiter` vào phiên làm việc của từng Socket client, tự động giải phóng đối tượng khi khối `try-with-resources` của socket kết thúc.

