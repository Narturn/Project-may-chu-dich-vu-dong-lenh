# GEMINI.md

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

---

## 5. Dự Án: Máy Chủ Dịch Vụ Dòng Lệnh (Đề Tài 31 - AJP201)

- **Môn học & Đề tài:** Lập trình Java nâng cao (AJP201) — Đề tài 31: Máy chủ dịch vụ dòng lệnh (Command-Line Service Server).
- **Tài liệu tham chiếu:**
  - Danh sách chức năng: `docs/danh-sach-chuc-nang.md` (16 chức năng F01–F16).
  - Sơ đồ thiết kế: thư mục `docs/so-do/` (Kiến trúc phân lớp, Sequence Diagrams, Lưu đồ lõi, Sơ đồ lớp UML).
- **Cấu trúc package và trách nhiệm các tầng:**
  - `mang`: Tầng Mạng & Giao tiếp (`CommandServer`, `ClientHandler`, `ConsoleClient`). Quản lý kết nối TCP, vòng đời Socket, tiếp nhận và phân phối luồng cho Worker, tương tác Console UI.
  - `giaothuc`: Tầng Phân tích & Giao thức (`ProtocolParser`, `Request`, `Response`, `StatusCode`, `ErrorCode`, `CommandException`). Phân tích chuỗi thô thành đối tượng bất biến, đóng gói thông điệp giao thức TCP qua Socket.
  - `nghiepvu`: Tầng Nghiệp vụ & Điều phối (`CommandDispatcher`, `CommandRegistry`, `KetQua`, `MetricsCollector`, `MetricsSnapshot`, các Handler nghiệp vụ F06, F07, F09, F13–F16). Điều phối và thực thi logic xử lý lệnh.
  - `luutru`: Tầng Dữ liệu & Lưu trữ (`MetricsRepository`). Chịu trách nhiệm đọc/ghi tệp `metrics.csv` bền vững trên ổ đĩa.
- **Tính độc lập của tầng nghiệp vụ:**
  - Tầng `nghiepvu` **hoàn toàn không phụ thuộc** vào tầng mạng (`Socket`, `ServerSocket`) hoặc giao diện (`ConsoleClient`), giúp kiểm thử đơn vị độc lập (JUnit 5) mà không cần bật máy chủ mạng.
- **Quy ước xử lý kết quả & lỗi:**
  - `KetQua`: `sealed interface` nội bộ của tầng nghiệp vụ biểu diễn 2 trạng thái: `KetQua.ThanhCong(String duLieu)` và `KetQua.ThatBai(ErrorCode maLoi, String thongDiep)`.
  - `CommandException`: Kế thừa `RuntimeException` (Unchecked), mang theo `ErrorCode` và thông điệp lỗi, đảm bảo tương thích 100% với `Function<String, String>` mà không vi phạm quy tắc biên dịch Lambda. `CommandDispatcher` bắt ngoại lệ này và chuyển thành `KetQua.ThatBai`.
  - `Response`: `record` thuộc tầng giao thức mạng, đóng gói thông điệp TCP trả về client qua Socket (`OK <payload>` hoặc `ERR <errorCode> <message>`).
  - `ClientHandler`: Đóng vai trò cầu nối tại tầng mạng, tiếp nhận `KetQua` từ `CommandDispatcher` và chuyển đổi thành `Response` trước khi gửi qua Socket.
- **Môi trường & Công nghệ:**
  - Sử dụng **Java 21 LTS** thuần (Java Standard Library).
  - **Không thêm bất kỳ thư viện bên ngoài nào** ngoài **JUnit 5** phục vụ kiểm thử đơn vị.
  - Ưu tiên tối đa các kỹ thuật Java nâng cao đã học trong slide Chương 1–6: Lambda & Functional Interface (Ch2), Streams API (Ch2), Collections & Generics (Ch3), Concurrency & Thread Pool (Ch4), Networking & Socket (Ch5), I/O & Custom Exceptions, Records, Sealed Interfaces, Pattern Matching (Ch6).
  - Quản lý tài nguyên: Bắt buộc đóng tệp, Socket, luồng I/O (`Reader`/`Writer`) đúng cách bằng `try-with-resources`.
  - Thiết kế hướng đối tượng: **Không sử dụng biến toàn cục** hoặc trạng thái tĩnh (static mutable state) không kiểm soát.
- **Quy trình triển khai:**
  - Mỗi lần **chỉ triển khai một chức năng** theo lộ trình tại `docs/danh-sach-chuc-nang.md`.
  - **Viết test trước khi triển khai chức năng (TDD):** Không tự ý sửa test chỉ để làm test vượt qua (test phải phản ánh đúng yêu cầu nghiệp vụ và tài liệu thiết kế).
- **Lệnh biên dịch và chạy (theo `README.md`):**
  - Biên dịch toàn bộ:
    ```bash
    mkdir -p bin
    javac -d bin $(find src -name "*.java")
    ```
  - Chạy Server:
    ```bash
    java -cp bin mang.CommandServer
    ```
  - Chạy Client:
    ```bash
    java -cp bin mang.ConsoleClient
    ```
- **Ngôn ngữ giao tiếp:** Luôn trả lời bằng **tiếng Việt**.

---

## 6. Quy Tắc Làm Việc Với Project

1. **Đọc trước khi sửa:** Luôn đọc kỹ tài liệu (`docs/`) và mã nguồn liên quan trước khi thực hiện bất kỳ thay đổi nào.
2. **Không vượt phạm vi:** Không tự ý thay đổi kiến trúc hệ thống, không tự ý đổi chữ ký phương thức đã thống nhất và không tự thêm tính năng ngoài yêu cầu đề bài.
3. **Phạm vi tác động tối thiểu:** Không chỉnh sửa những file không liên quan đến chức năng đang thực hiện.
4. **Đơn giản & Chuẩn mực:** Giữ code rõ ràng, dễ đọc, cấu trúc tường minh và phù hợp với chuẩn mực học phần AJP201.
5. **Kiểm tra sau thay đổi:** Sau mỗi lần chỉnh sửa, thực hiện biên dịch hoặc chạy test phù hợp để đảm bảo không phát sinh lỗi biên dịch hay hồi quy.
6. **Báo cáo rõ ràng:** Khi hoàn thành, giải thích ngắn gọn, súc tích những thay đổi quan trọng đã thực hiện.

