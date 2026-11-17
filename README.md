# VetCare – Veterinary Clinic Management System

Ứng dụng quản lý phòng khám thú y trên máy tính, xây dựng bằng **Java Swing**, **FlatLaf**, **Maven** và **SQLite**. Giao diện sử dụng tiếng Anh; tài liệu hướng dẫn và kịch bản kiểm thử sử dụng tiếng Việt.

## 1. Hướng dẫn cài đặt và chạy ứng dụng

### 1.1. Yêu cầu

- **JDK 17 trở lên** (khuyến nghị JDK 21).
- **Apache Maven** đã được cài đặt và thêm vào `PATH`.
- Windows, macOS hoặc Linux có môi trường đồ họa; có thể mở dự án bằng VS Code.

Kiểm tra môi trường trong Terminal/PowerShell:

```powershell
java -version
mvn -version
```

### 1.2. Chạy từ source code

1. Tải ZIP từ GitHub và giải nén, hoặc clone repository:

   ```powershell
   git clone https://github.com/ThanhTien35/VetCare-Clinic-Management.git
   cd VetCare-Clinic-Management
   ```


2. Chạy lệnh:

   ```powershell
   mvn clean compile exec:java
   ```

3. Đợi cửa sổ **VetCare Login** xuất hiện và đăng nhập bằng tài khoản demo bên dưới.

> **SQLite:** Ứng dụng sử dụng file `vetcare.db` tại **thư mục làm việc hiện tại**. Khi chạy lần đầu mà chưa có file này, chương trình sẽ tạo database và các bảng cần thiết. **Không xóa `vetcare.db` nếu muốn giữ dữ liệu.** Luôn chạy Maven từ cùng một thư mục để tránh tạo nhiều database ở các vị trí khác nhau.
>

> Nếu đã chạy bản cũ có dữ liệu, nên sao lưu `vetcare.db` trước khi thay source code hoặc thử nghiệm. Database và thư mục `target/` không cần đưa lên GitHub.

### 1.3. Tài khoản đăng nhập demo

| Vai trò | Username | Password |
|---|---|---|
| **Admin** | `admin` | `admin123` |
| **Receptionist** | `receptionist` | `reception123` |
| **Veterinarian** | `vet` | `vet123` |

> Các tài khoản được khai báo cố định trong mã nguồn để phục vụ demo môn học, **không phù hợp để triển khai thực tế**.

## 2. Chức năng theo từng tài khoản

| Menu / Chức năng | Admin | Receptionist | Veterinarian |
|---|:---:|:---:|:---:|
| Dashboard | ✓ | ✓ | ✓ |
| Owners & Pets | ✓ | ✓ | — |
| Appointments | ✓ | ✓ | — |
| Check-in & Queue | ✓ | ✓ | — |
| Invoices | ✓ | ✓ | — |
| Examination / EMR | — | — | ✓ |
| Prescriptions | — | — | ✓ |
| Vaccinations | — | — | ✓ |
| Services | ✓ | — | — |
| Staff Accounts | Placeholder | — | — |

**Lưu ý:** Menu **Staff Accounts** hiện chỉ là giao diện placeholder, chưa có chức năng tạo/sửa tài khoản. Các chỉ số minh họa trên Dashboard không nhất thiết phản ánh dữ liệu SQLite thực tế.

## 3. Kịch bản kiểm thử toàn hệ thống (TC01–TC13)

**Điều kiện:** Chạy ứng dụng thành công, có thể đăng nhập cả ba tài khoản. Nên sử dụng dữ liệu thử mới để dễ phân biệt với dữ liệu demo có sẵn. Nếu số điện thoại ví dụ đã tồn tại, hãy dùng số thử khác chưa có trong hệ thống.

### TC01 – Authentication (Admin)

1. Đăng nhập `admin` / `admin123`.
2. Kiểm tra Dashboard xuất hiện.
3. Đăng xuất; thử đăng nhập với mật khẩu sai.

**Mong đợi:** Đăng nhập đúng thành công; đăng nhập sai bị từ chối.

### TC02 – Owners (Admin hoặc Receptionist)

1. Đăng nhập **Receptionist** (`receptionist` / `reception123`).
2. Mở **Owners & Pets** và thêm chủ nuôi: `Test Owner`, điện thoại `0987654321`, email `test@example.com`.
3. Thử thêm chủ nuôi thứ hai với cùng số điện thoại.

**Mong đợi:** Chủ nuôi được tạo thành công lần đầu; số điện thoại trùng bị từ chối.

### TC03 – Pets (Receptionist)

1. Trong **Owners & Pets**, thêm thú cưng thuộc `Test Owner`.
2. Nhập: tên `Lucky`, loài `Dog`, giống `Poodle`, tuổi `2`.

**Mong đợi:** Lucky xuất hiện và liên kết đúng chủ nuôi.

### TC04 – Appointments (Receptionist)

1. Mở **Appointments**, tạo lịch hẹn cho `Lucky`.
2. Chọn bác sĩ `Dr. Nguyen Minh` và một ngày/giờ **trong tương lai**.
3. Ghi lại mã lịch hẹn để sử dụng cho các test tiếp theo.

**Mong đợi:** Lịch hẹn được tạo với trạng thái `Scheduled`.

### TC05 – Check-in (Receptionist)

1. Mở **Check-in & Queue**.
2. Chọn lịch hẹn của Lucky, nhập cân nặng `5.5` kg, ghi chú `Healthy`.
3. Xác nhận check-in.

**Mong đợi:** Trạng thái chuyển `Checked In`; thông tin check-in được ghi nhận.

### TC06 – Examination / EMR (Veterinarian)

1. Nhấn **Log out**, đăng nhập `vet` / `vet123`.
2. Mở **Examination / EMR**, chọn lịch hẹn Lucky đã check-in.
3. Nhấn **Start Examination**, nhập:
   - Symptoms: `Cough`
   - Diagnosis: `Mild infection`
   - Treatment: `Medication and rest`
4. Lưu hồ sơ khám.

**Mong đợi:** Medical Record được tạo; lịch hẹn chuyển sang `Completed`.

### TC07 – Prescriptions (Veterinarian)

1. Mở **Prescriptions**, nhấn **+ Create Prescription**.
2. Chọn hồ sơ khám của Lucky vừa hoàn thành.
3. Thêm thuốc với dữ liệu ví dụ:
   - Medicine: `Amoxicillin`
   - Dosage: `1 tablet`
   - Frequency: `Twice daily`
   - Days: `5`
4. Lưu và dùng **View Prescription** để kiểm tra.

**Mong đợi:** Đơn thuốc được tạo và hiển thị đúng các trường đã nhập. *Dữ liệu thuốc chỉ phục vụ kiểm thử phần mềm, không phải hướng dẫn điều trị thú y.*

### TC08 – Vaccinations (Veterinarian)

1. Mở **Vaccinations**, nhấn **+ Record Vaccination**.
2. Chọn Lucky, vaccine `Rabies`, ngày tiêm là ngày hiện tại, ngày nhắc lại sau một năm.
3. Lưu và kiểm tra **View Details**.

**Mong đợi:** Lịch sử tiêm chủng và ngày nhắc lại được ghi nhận.

### TC09 – Services (Admin)

1. Đăng xuất, đăng nhập `admin` / `admin123`.
2. Mở **Services**, kiểm tra dịch vụ `General Examination`.
3. Nhấn **+ Add Service**, nhập `Test Service`, giá `100000` VND và lưu.

**Mong đợi:** Dịch vụ mới xuất hiện; dịch vụ có sẵn vẫn được giữ nguyên.

### TC10 – Invoices (Admin hoặc Receptionist)

1. Trong tài khoản **Admin**, mở **Invoices**.
2. Nhấn **+ Create Invoice**, chọn lịch hẹn của Lucky đã `Completed`.
3. Thêm dịch vụ `General Examination` với số lượng `1`.
4. Xác nhận tạo hóa đơn.

**Mong đợi:** Hóa đơn có trạng thái `Unpaid`; tổng tiền bằng giá dịch vụ nhân số lượng (mặc định `150000` VND nếu giá chưa bị thay đổi).

### TC11 – Payment (Admin hoặc Receptionist)

1. Trong **Invoices**, chọn hóa đơn vừa tạo.
2. Nhấn **Record Payment**, chọn `Cash` và xác nhận.
3. Thử thanh toán lại hóa đơn đó.

**Mong đợi:** Trạng thái chuyển `Paid`, lưu phương thức thanh toán; không cho thanh toán lại.

### TC12 – SQLite Persistence (kiểm tra bằng cả ba tài khoản)

1. **Đóng hoàn toàn** ứng dụng.
2. Tại cùng thư mục chứa `pom.xml`, chạy lại:

   ```powershell
   mvn clean compile exec:java
   ```

3. Đăng nhập **Receptionist** hoặc **Admin**: kiểm tra Owner, Lucky, lịch hẹn, dịch vụ (Admin), hóa đơn và trạng thái `Paid`.
4. Đăng xuất, đăng nhập **Veterinarian**: kiểm tra Medical Record, Prescription và Vaccination.

**Mong đợi:** Tất cả dữ liệu vừa tạo vẫn còn; trạng thái `Completed` và `Paid` được giữ nguyên sau khi khởi động lại.

### TC13 – Role Permissions (cả ba tài khoản)

1. Đăng nhập **Admin**: kiểm tra các menu Owners & Pets, Appointments, Check-in & Queue, Invoices, Services, Staff Accounts.
2. Đăng nhập **Receptionist**: kiểm tra các menu Owners & Pets, Appointments, Check-in & Queue, Invoices; không thấy Services hoặc các menu chuyên môn bác sĩ.
3. Đăng nhập **Veterinarian**: kiểm tra các menu Examination / EMR, Prescriptions, Vaccinations; không thấy Services hoặc Invoices.

**Mong đợi:** Menu hiển thị đúng theo bảng phân quyền ở Mục 2. Đây là kiểm thử phân quyền **qua giao diện**, không phải đánh giá bảo mật ở mức backend.

### Bảng tổng hợp kết quả kiểm thử

Kết quả do nhóm ghi nhận sau khi thực hiện thủ công:

| Test case | Chức năng | Kết quả |
|---|---|---|
| TC01 | Authentication | PASS |
| TC02 | Owners | PASS |
| TC03 | Pets | PASS |
| TC04 | Appointments | PASS |
| TC05 | Check-in | PASS |
| TC06 | Examination / EMR | PASS |
| TC07 | Prescriptions | PASS |
| TC08 | Vaccinations | PASS |
| TC09 | Services | PASS |
| TC10 | Invoices | PASS |
| TC11 | Payment | PASS |
| TC12 | SQLite Persistence | PASS |
| TC13 | Role Permissions | PASS |

**Tổng kết:** `13/13 PASS` trong đợt kiểm thử thủ công được báo cáo. Kết quả không thay thế kiểm thử tự động, bảo mật hay kiểm thử trên mọi môi trường.

## 4. Quy trình demo nhanh

```text
Receptionist: Owners & Pets → Appointments → Check-in & Queue
                               ↓
Veterinarian: Examination / EMR → Prescriptions → Vaccinations
                               ↓
Admin:        Services → Invoices → Record Payment
                               ↓
Restart app:  Verify SQLite persistence with each role
```

## 5. Công nghệ và cấu trúc dự án

- **Java 17+**, Java Swing, FlatLaf 3.6.1
- **Maven** (compiler và exec plugin)
- **SQLite** với `sqlite-jdbc` 3.49.1.0
- Source chính: `src/main/java/com/vetcare/`
- Điểm khởi chạy: `com.vetcare.Main`
- Quản lý dữ liệu: `ClinicData.java`, `DatabaseManager.java`

