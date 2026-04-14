# 🗂️ Flip Flashcard - Learning App

**Flip Flashcard** là một ứng dụng di động mạnh mẽ được thiết kế để tối ưu hóa việc ghi nhớ thông qua các thẻ ghi nhớ (Flashcards). Ứng dụng kết hợp giữa thuật toán học tập khoa học (SM2) và một giao diện hiện đại theo phong cách Neo-Brutalism.

## 🚀 Các Tính Năng Cốt Lõi

-   **Hệ thống học tập SM2**: Sử dụng thuật toán lặp lại ngắt quãng (Spaced Repetition) để tối ưu hóa thời gian ghi nhớ.
-   **Đồng bộ hóa thời gian thực (Real-time Sync)**: Tự động đồng bộ hóa bộ thẻ và lịch sử học tập giữa nhiều thiết bị qua Firebase Firestore.
-   **Single Device Login**: Bảo mật tài khoản bằng cách chỉ cho phép đăng nhập trên một thiết bị duy nhất tại một thời điểm.
-   **Thống kê chuyên sâu**: Theo dõi Streak, số thẻ đã thuộc, và dự báo lịch ôn tập trong tương lai.
-   **Neo-Brutalism UI**: Giao diện độc đáo với màu sắc tương phản mạnh, viền đậm và các micro-animations mượt mà.
-   **Nhắc nhở hàng ngày**: Tích hợp WorkManager để gửi thông báo ôn tập đúng giờ.

## 🛠️ Stack Công Nghệ

-   **Language**: Kotlin
-   **UI Framework**: Jetpack Compose
-   **Architecture**: MVVM (Model-View-ViewModel)
-   **Dependency Injection**: Hilt
-   **Database Local**: Room Database (Version 5 - Hỗ trợ Migration)
-   **Cloud/Auth**: Firebase (Auth & Firestore Snapshot Listener)
-   **Scheduling**: WorkManager

## 📁 Cấu Trúc Dự Án

-   `data/`: Chứa các thực thể Database (Room), DAO, và triển khai Repository.
-   `domain/`: Chứa các Interface Repository và Model dùng chung.
-   `ui/`:
    -   `screens/`: Các màn hình chính (Home, Decks, Study, Stats, Settings, Login).
    -   `theme/`: Hệ thống màu sắc và kiểu dáng Neo-Brutalism.
    -   `viewmodel/`: Logic xử lý dữ liệu cho UI.
-   `utils/`: Các tiện ích như thuật toán SM2, xử lý CSV, định dạng ngày tháng.

## ⚙️ Cài Đặt

1.  Clone repository về máy:
    ```bash
    git clone https://github.com/AnhPha1704/flashcard.git
    ```
2.  Mở dự án bằng **Android Studio (Koala hoặc mới hơn)**.
3.  Kết nối với Firebase:
    -   Thêm file `google-services.json` vào thư mục `app/`.
    -   Cấu hình Firebase Auth (Email/Password) và Firestore.
4.  Build và chạy ứng dụng trên Emulator hoặc thiết bị thật.

## 🎨 Design System

Ứng dụng sử dụng bảng màu đặc trưng:
-   **NeoNavy (#000033)**: Màu chủ đạo cho text và viền.
-   **NeoBackgroundPink (#FFD1DC)**: Màu nền nhẹ nhàng nhưng cá tính.
-   **NeoWhite (#FFFFFF)**: Màu nền cho các bề mặt nổi khối.

---
Phát triển bởi **AnhPha** và **LyThiHoa**.
