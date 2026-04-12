# 🚀 Lộ trình Phát triển Ứng dụng Flashcard (Đề tài 19)

---

## 📅 Các giai đoạn chi tiết

### 🏗️ Giai đoạn 1: Khởi tạo & Cấu trúc (Tuần 1)
> [!IMPORTANT]
> **Mục tiêu:** Thiết lập "móng" vững chắc theo chuẩn chuyên nghiệp.

#### 🔧 GitHub Workflow
- [x] Thiết lập nhánh `develop` làm nhánh mặc định.
- [x] Thiết lập quy tắc bảo vệ nhánh (**Branch Protection**): bắt buộc phải có PR và Review mới được merge code.

#### 🏢 Kiến trúc (MVVM)
- [x] Khởi tạo dự án Android Studio với **Jetpack Compose**.
- [x] Cài đặt **Hilt** để quản lý Dependency Injection.
- [x] Phân chia cấu trúc Package rõ ràng:
    - `data`: Room Database, API Service (Dữ liệu).
    - `domain`: SM-2 Logic, Repository Interfaces (Nghiệp vụ).
    - `ui`: Compose Screens, ViewModels, Theme (Giao diện).
    
---

### 🧠 Giai đoạn 2: Xây dựng "Bộ não" (Tuần 2)
> [!NOTE]
> **Mục tiêu:** Hoàn thành logic cốt lõi và hệ thống lưu trữ dữ liệu bền vững.

#### 💾 Database (Room)
- [x ] Thiết kế bảng `Flashcard` và `Deck`.
- [x ] Đảm bảo hỗ trợ chế độ học **Offline-first**.

#### ⚡ Thuật toán SM-2
- [x] Hiện thực hóa logic tính toán khoảng cách ngày ôn tập (Interval) dựa trên mức độ đánh giá của người dùng.
- [x] **Unit Test:** Viết test suite cho class SM-2 để đảm bảo các trường hợp "Dễ", "Trung bình", "Khó" tính toán chính xác ngày tái hiện.

#### 🔄 Quy trình Git
- [ ] Mỗi tính năng phải được phát triển trên nhánh riêng: `feature/[tên-tính-năng]-[tên-thành-viên]`.
- [ ] Thực hiện Pull Request để đồng nghiệp review code trước khi merge.

---

### 🎨 Giai đoạn 3: Giao diện (UI) & Trải nghiệm (UX) (Tuần 3)
> [!TIP]
> **Mục tiêu:** Tạo ứng dụng có giao diện hiện đại, mượt mà và trực quan.

- [x ] **Màn hình chính:** Danh sách bộ thẻ (Deck) với hiệu ứng cuộn mượt mà bằng `LazyColumn`.
- [ ] **Màn hình học tập:**
    - [ x] Thiết kế hiệu ứng lật thẻ (**Card Flip Animation**) bằng Compose `graphicsLayer`.
    - [ x] Áp dụng hệ thống màu sắc và font chữ theo chuẩn **Material Design 3**.
- [ ] **Tương tác:** Tích hợp cử chỉ vuốt (**Swipe**) để đánh dấu trạng thái thuộc bài nhanh chóng.

---

### 🛠️ Giai đoạn 4: Tính năng nâng cao & Cloud Sync (Tuần 4)
> [!WARNING]
> **Mục tiêu:** Hoàn thiện các yêu cầu kỹ thuật đặc thù của đề tài.

- [ ] **WorkManager:** Lên lịch thông báo (**Push Notification**) nhắc nhở việc học hàng ngày theo khung giờ vàng.
- [ ] **Text-to-Speech (TTS):** Tích hợp Google TTS giúp người dùng phát âm từ vựng chuẩn xác.
- [ ] **Cloud Sync:** Kết nối với Backend để đồng bộ hóa tiến độ học tập trên nhiều thiết bị.
- [ ] **Error Handling:** Xử lý triệt để các trường hợp mất kết nối mạng hoặc lỗi API để ứng dụng luôn ổn định.

---

### 🏁 Giai đoạn 5: Đóng gói & Hoàn thiện (Tuần cuối)
> [!CAUTION]
> **Mục tiêu:** Hoàn tất hồ sơ, kiểm tra chất lượng và chuẩn bị báo cáo.

- [ ] **Tài liệu:** Hoàn thiện file `README.md` (mô tả cấu trúc DB, sơ đồ API, luồng dữ liệu) và slide thuyết trình.
- [ ] **Video Demo:** Quay clip giới thiệu các tính năng nổi bật và đăng tải lên YouTube.
- [ ] **Quảng bá:** Viết bài giới thiệu sản phẩm lên cộng đồng (Facebook/LinkedIn) để nhận phản hồi và bình chọn.
- [ ] **Kiểm soát đóng góp:** Kiểm tra `Insights -> Contributors` để đảm bảo tỷ lệ đóng góp của các thành viên công bằng (tối thiểu 15-20%).