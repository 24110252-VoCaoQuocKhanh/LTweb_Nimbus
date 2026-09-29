# JWT Spring Boot + MySQL

Ví dụ được triển khai theo bài giảng `04_JWT.pdf`: đăng ký, đăng nhập sinh JWT, JWT filter, Spring Security stateless, API người dùng, xử lý lỗi và giao diện AJAX. Phiên bản này dùng **JJWT 0.12.6** đúng với slide; Nimbus chưa được dùng trong yêu cầu hiện tại.

## Cấu hình mặc định

- Ứng dụng: `http://localhost:8005`
- Database: `jwt_nimbus` tại `localhost:3306`
- MySQL user: `root`
- Hibernate tự tạo/cập nhật bảng `users`.

Các giá trị có thể ghi đè bằng biến môi trường: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_TIME`, `JWT_ISSUER`.

## Chạy ứng dụng

```powershell
.\mvnw.cmd spring-boot:run
```

Mở `http://localhost:8005/register` để tạo tài khoản, sau đó đăng nhập tại `http://localhost:8005/login`.

## API

### Đăng ký

```http
POST /auth/signup
Content-Type: application/json

{
  "fullName": "Nguyen Van A",
  "email": "a@example.com",
  "password": "123456"
}
```

### Đăng nhập

```http
POST /auth/login
Content-Type: application/json

{
  "email": "a@example.com",
  "password": "123456"
}
```

### API cần JWT

```http
GET /users/me
Authorization: Bearer <token>
```

```http
GET /users
Authorization: Bearer <token>
```
