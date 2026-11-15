# Máy Chủ Dịch Vụ Dòng Lệnh (Command-Line Service Server)
## Đề Tài 31 — Học Phần Lập Trình Java Nâng Cao (AJP201)

Hệ thống máy chủ dịch vụ dòng lệnh đa luồng qua giao thức mạng TCP/IP bằng Java 21 thuần (Standard Library, không phụ thuộc framework bên thứ ba ngoài JUnit 5 cho kiểm thử). Hệ thống tuân thủ kiến trúc phân tầng, nguyên tắc phân tách trách nhiệm (Separation of Concerns), an toàn luồng và xử lý lỗi chuẩn mực.

---

## 1. Cấu Trúc Dự Án (Project Structure)

Dự án được tổ chức theo kiến trúc 3 tầng chuẩn mực kết hợp mô hình chia sẻ:

```
Project-may-chu-dich-vu-dong-lenh/
├── src/                               # Toàn bộ mã nguồn Java 21
│   ├── giaothuc/                      # Tầng Phân tích & Giao thức (Protocol & Shared Models)
│   │   ├── CommandException.java      # Custom unchecked exception (mang ErrorCode)
│   │   ├── ErrorCode.java             # Danh mục mã lỗi enum chuẩn
│   │   ├── ProtocolParser.java        # Phân tích cú pháp dòng lệnh thô thành Request
│   │   ├── Request.java               # Record bất biến biểu diễn bản tin yêu cầu
│   │   ├── Response.java              # Record bản tin phản hồi giao thức TCP (OK/ERR)
│   │   └── StatusCode.java            # Enum trạng thái phản hồi (OK, ERR)
│   ├── nghiepvu/                      # Tầng Nghiệp vụ & Điều phối (Core Domain & Dispatching)
│   │   ├── KetQua.java                # Sealed interface kết quả nội bộ (ThanhCong, ThatBai)
│   │   ├── CommandRegistry.java       # Đăng ký & tra cứu Map<String, Function<String, String>>
│   │   ├── CommandDispatcher.java     # Điều phối lệnh, bắt ngoại lệ & trả KetQua
│   │   ├── MetricsCollector.java      # Bộ đếm thống kê an toàn luồng (In-memory state)
│   │   ├── MetricsSnapshot.java       # Record sao chụp bất biến dữ liệu thống kê
│   │   ├── TimeHandler.java           # Xử lý lệnh TIME và DATE (F06)
│   │   ├── EchoHandler.java           # Xử lý lệnh ECHO (F07)
│   │   ├── UpperHandler.java          # Xử lý lệnh UPPER (F07)
│   │   ├── StatsHandler.java          # Xử lý lệnh STATS bằng Streams API (F09)
│   │   ├── CalcHandler.java           # Xử lý biểu thức CALC (F13 - Mở rộng)
│   │   ├── HelpHandler.java           # Trợ giúp lệnh HELP (F16 - Mở rộng)
│   │   ├── SessionHistoryManager.java # Quản lý lịch sử lệnh theo phiên (F14 - Mở rộng)
│   │   └── RateLimiter.java           # Giới hạn tần suất gọi lệnh (F15 - Mở rộng)
│   ├── luutru/                        # Tầng Dữ liệu & Lưu trữ (Data & Persistence)
│   │   └── MetricsRepository.java     # Đọc/ghi CSV qua MetricsSnapshot với try-with-resources
│   └── mang/                          # Tầng Mạng & Giao tiếp (Network & Presentation)
│       ├── CommandServer.java         # Máy chủ ServerSocket + ExecutorService + setSoTimeout
│       ├── ClientHandler.java         # Worker xử lý phiên kết nối của một client
│       └── ConsoleClient.java         # Ứng dụng khách dòng lệnh tương tác (Console UI)
├── docs/                              # Tài liệu phân tích và thiết kế
│   ├── danh-sach-chuc-nang.md         # Phân tích 16 chức năng (F01–F16)
│   └── so-do/                         # Kế hoạch thiết kế, rà soát và sơ đồ Draw.io
├── data/                              # Thư mục lưu trữ tệp số liệu bền vững (metrics.csv)
├── test/                              # Thư mục chứa các ca kiểm thử JUnit 5
└── reference/                         # Tài liệu học phần và kế hoạch triển khai
```

---

## 2. Các Quyết Định Kỹ Thuật Đã Thống Nhất

1. **Custom Exception:** `CommandException extends RuntimeException`, mang `ErrorCode` và thông điệp lỗi, tương thích 100% với `Function<String, String>` mà không vi phạm quy tắc biên dịch Lambda.
2. **Phân tách kết quả:** `CommandDispatcher` trả về `KetQua` (`KetQua.ThanhCong` / `KetQua.ThatBai`) nội bộ cho tầng nghiệp vụ. `ClientHandler` chịu trách nhiệm chuyển đổi sang `Response` (TCP protocol message).
3. **Phân tầng Metrics:** `MetricsCollector` thuộc package `nghiepvu` quản lý bộ đếm an toàn luồng trong RAM và tạo `MetricsSnapshot`; `MetricsRepository` thuộc package `luutru` đọc/ghi `metrics.csv` qua `MetricsSnapshot`.
4. **Mô hình đa luồng:** `ExecutorService` với Bể luồng cố định (mặc định 10 workers) kết hợp `Socket.setSoTimeout(60_000)` để giải phóng worker khi client nhàn rỗi, tránh nghẽn hàng đợi `LinkedBlockingQueue`.
5. **Mã lỗi Rate Limiter:** Thống nhất dùng `ErrorCode.ERR_RATE_LIMIT_EXCEEDED`.
6. **Phương thức Snapshot:** Thống nhất tên phương thức `MetricsCollector.getSnapshot()`.
7. **Bản tin Response:** Sử dụng `record Response(StatusCode status, String payload)`.

---

## 3. Yêu Cầu Môi Trường

- **Java Development Kit (JDK):** Java 21 LTS (Oracle JDK, Eclipse Temurin hoặc OpenJDK 21).
- Kiểm tra phiên bản môi trường:
  ```bash
  java -version
  javac -version
  ```
  *(Cả hai lệnh đều phải báo phiên bản `21.x.x`)*.

---

## 4. Biên Dịch & Chạy Chương Trình

### 4.1. Biên dịch toàn bộ mã nguồn

Mở terminal tại thư mục gốc của dự án (`Project-may-chu-dich-vu-dong-lenh`):

- **Trên Linux / macOS (Bash):**
  ```bash
  mkdir -p bin
  javac -d bin $(find src -name "*.java")
  ```

- **Trên Windows (PowerShell):**
  ```powershell
  if (!(Test-Path bin)) { New-Item -ItemType Directory -Path bin }
  javac -d bin (Get-ChildItem -Path src -Recurse -Filter *.java).FullName
  ```

- **Trên Windows (CMD):**
  ```cmd
  if not exist bin mkdir bin
  dir /s /b src\*.java > sources.txt
  javac -d bin @sources.txt
  del sources.txt
  ```

---

### 4.2. Khởi chạy Máy chủ (CommandServer)

Sau khi biên dịch thành công vào thư mục `bin`:

```bash
java -cp bin mang.CommandServer
```

Máy chủ sẽ lắng nghe mặc định trên cổng `8080` (hoặc cổng được chỉ định qua tham số dòng lệnh).

---

### 4.3. Khởi chạy Máy khách (ConsoleClient)

Mở một cửa sổ terminal mới và chạy:

```bash
java -cp bin mang.ConsoleClient
```

Mặc định máy khách sẽ kết nối tới `localhost:8080`.

---

### 4.4. Biên dịch và Chạy kiểm thử đơn vị (Unit Tests - JUnit 5)

Dự án sử dụng file standalone `junit-platform-console-standalone-1.14.4.jar` đặt tại thư mục `lib/`.

- **Biên dịch cả mã nguồn (`src/`) và kiểm thử (`test/`):**
  ```bash
  mkdir -p bin
  javac -cp "lib/junit-platform-console-standalone-1.14.4.jar:bin" -d bin $(find src test -name "*.java")
  ```

- **Thực thi toàn bộ kiểm thử đơn vị:**
  ```bash
  java -jar lib/junit-platform-console-standalone-1.14.4.jar execute --class-path bin --scan-class-path
  ```


