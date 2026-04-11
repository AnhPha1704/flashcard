# 📚 Đề tài và Yêu cầu Đồ án

## 🛠️ Phần 1: Quy định bắt buộc về Quản lý mã nguồn (GitHub Workflow)
> [!IMPORTANT]
> Việc tuân thủ quy trình này là bắt buộc để đánh giá kỹ năng làm việc nhóm và quản lý dự án chuyên nghiệp.

### 1. Mô hình phân nhánh (Branching Strategy)
- **Tuyệt đối không push code trực tiếp** lên nhánh `main` (hoặc `master`).
- **Nhánh `main`:** Chỉ chứa code hoàn chỉnh, ổn định và có thể chạy được (production-ready).
- **Nhánh `develop`:** Nhánh hội tụ code của cả nhóm trong quá trình phát triển.
- **Nhánh tính năng (`feature/`):** Khi làm một chức năng mới, sinh viên **BẮT BUỘC** phải tạo nhánh mới từ `develop`.
    - *Quy tắc đặt tên nhánh:* `feature/[tên-chức-năng]-[tên-sinh-viên]` (Ví dụ: `feature/login-api-nguyenvanA`).

### 2. Quy định về Commit
- **Chia nhỏ commit:** Không được dồn toàn bộ code làm trong nhiều ngày vào một commit duy nhất. Mỗi commit chỉ nên giải quyết một vấn đề hoặc một phần nhỏ của chức năng.
- **Thông điệp commit (Commit Message):** Phải rõ ràng, viết bằng tiếng Anh. Theo chuẩn: `[Loại]: Mô tả ngắn gọn` (Ví dụ: `feat: add Login UI`, `fix: crash on fetching API`, `docs: update README`).

### 3. Quy định về Gộp code (Merge) & Pull Request (PR)
- Khi hoàn thành chức năng trên nhánh feature, sinh viên phải tạo **Pull Request (PR)** để gộp vào nhánh develop.
- **Bắt buộc Review:** Trong PR, phải có ít nhất 1 thành viên khác trong nhóm xem xét (review), approve và thực hiện merge.
- **Xử lý xung đột (Merge Conflict):** Quá trình giải quyết conflict phải được thảo luận trong nhóm. Các PR giải quyết conflict tốt sẽ là điểm cộng.

### 4. Đánh giá đóng góp (Contribution & Insights)
- Kiểm tra mục `Insights -> Contributors` trên GitHub repository. Biểu đồ đóng góp phải thể hiện sự cân bằng tương đối giữa các thành viên.
- Nếu một thành viên có lượng đóng góp dưới **15%** tổng khối lượng dự án mà không có lý do chính đáng, sẽ bị đánh giá thấp trong phần thống kê.

---

## 📊 Phần 2: Bảng Tiêu chí Đánh giá Đồ án (Rubric)
*Thang điểm 100%*

| Tiêu chí | Xuất sắc (90 - 100%) | Khá (70 - 89%) | Trung bình (50 - 69%) | Yếu (Dưới 50%) | Trọng số |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1. Quản lý mã nguồn** | Tuân thủ tuyệt đối Git Flow. PRs chi tiết, có peer-review. Commit rải đều. Đóng góp đồng đều. | Có chia nhánh và PR nhưng đôi khi còn push thẳng lên develop. Đóng góp tương đối đều. | Ít sử dụng nhánh, commit cục bộ (1-2 commit lớn). 1 người gánh team merge code. | Không dùng Git/GitHub hoặc chỉ dùng như nơi lưu trữ zip. | **20%** |
| **2. Kiến trúc & Code Quality** | Áp dụng Clean Architecture / MVVM. Dùng Hilt mượt mà. Xử lý bất đồng bộ tốt (Coroutines/Flow). | Có dùng MVVM nhưng chưa tách biệt rõ UI và Logic. Setup Dependency Injection cơ bản. | Code dồn vào Activity/Fragment. Không áp dụng kiến trúc, khó bảo trì. | Code chạy lỗi, crash liên tục. Vi phạm nghiêm trọng SOLID. | **20%** |
| **3. Hoàn thiện Chức năng & API** | Tính năng phức tạp ổn định. Tương tác sâu với Backend. Xử lý tốt offline (Room DB). | Chức năng chính hoạt động tốt. Có gọi API nhưng chưa xử lý tốt các luồng lỗi. | Hoạt động chập chờn. Chỉ dùng DB cục bộ đơn giản hoặc API public không tùy biến. | Thiếu chức năng cốt lõi. Giao diện vỡ, không gọi được dữ liệu. | **30%** |
| **4. Giao diện, UX & Hiệu năng** | Giao diện hiện đại (Jetpack Compose), animation mượt. Tối ưu list tốt. | Gọn gàng, đúng chuẩn Material Design. Không có lỗi UI nghiêm trọng. | Giao diện sơ sài, dùng mặc định. Load dữ liệu chậm làm block UI. | Giao diện lỗi thời, vỡ layout trên các màn hình khác nhau. | **15%** |
| **5. Báo cáo & Thuyết trình** | Tài liệu rõ ràng. Demo trôi chảy. Trả lời xuất sắc các câu hỏi phản biện từ giảng viên. | Tài liệu đầy đủ. Demo được chức năng chính. Trả lời được hầu hết câu hỏi. | Tài liệu sơ sài. Lúc demo xảy ra lỗi. Không giải thích được luồng hoạt động của code. | Không có báo cáo. Không tham gia thuyết trình hoặc không trả lời được câu hỏi. | **15%** |

### 🌟 Điểm cộng (Bonus) - Lên đến +10%
- [ ] Sử dụng **CI/CD** (GitHub Actions) để tự động build file APK.
- [ ] Triển khai viết **Unit Test** (JUnit, Mockito) cho Logic/ViewModel.
- [ ] Tính năng đột phá (AI On-device, IoT, giao tiếp Real-time xuất sắc).

---

## 💡 Phần 3: Chi tiết Đề tài
### 🎓 19. Ứng dụng Học tập qua Flashcard
> [!NOTE]
> **Mô tả:** Giống Anki/Quizlet, cho phép tạo bộ thẻ từ vựng và tự động lên lịch ôn tập dựa trên độ khó.

#### ✅ Yêu cầu nâng cao
- Hiện thực hóa thuật toán **SuperMemo (SM-2)** hoặc tương tự để tối ưu việc ghi nhớ.
- Sử dụng **WorkManager** để lên lịch nhắc nhở học bài hàng ngày theo thời gian thực.

#### 🌐 Nguồn dữ liệu/API
- **Backend:** Lưu trữ tiến độ học tập để hỗ trợ đồng bộ chéo thiết bị.
- **Google TTS API:** Tích hợp Text-to-Speech để phát âm chuẩn các từ vựng trên thẻ.