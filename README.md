# 🔐 BabyVo Authentication & Authorization

Bu doküman BabyVo backend projesindeki **login / authentication** altyapısını açıklar.

Desteklenen giriş yöntemleri:
- ✅ **Email + OTP**
- ✅ **Google Sign-In**
- ⏳ **Apple Sign-In** (sonraki adım)

Amaç:
- Stateless
- JWT tabanlı
- Redis destekli
- Production-ready bir auth altyapısı

---

## İçindekiler
- Genel Mimari
- Token Modeli
- Email + OTP Login
- Google Login
- Refresh Token Mekanizması
- Logout
- Logout All Devices
- Redis Yapısı
- Güvenlik Notları
- cURL Örnekleri
- Konfigürasyon
- Yol Haritası

---

## Genel Mimari

BabyVo authentication sistemi **JWT + Redis** yaklaşımıyla tasarlanmıştır.

- Access token: kısa ömürlü, stateless
- Refresh token: uzun ömürlü, Redis ile kontrol edilen
- Tüm login yöntemleri aynı token altyapısını kullanır

---

## Token Modeli

### Access Token
- API çağrıları için kullanılır
- Kısa ömürlüdür (default 15 dk)
- Redis’te tutulmaz
- Header üzerinden gönderilir

```
Authorization: Bearer <accessToken>
```

### Refresh Token
- Access token süresi dolduğunda yenileme için kullanılır
- Uzun ömürlüdür (default 30 gün)
- Redis’te aktiflik ve reuse kontrolü yapılır
- Her kullanımda **rotation** uygulanır

---

## Email + OTP Login

Email login akışı iki adımdan oluşur.

### 1) OTP Başlat

```
POST /api/v1/auth/email/start
```

Request:
```json
{
  "email": "user@example.com"
}
```

Response:
```json
{
  "success": true,
  "data": {
    "otpRef": "UUID",
    "expiresInSeconds": 180
  }
}
```

Bu adımda:
- OTP üretilir
- Hash’lenerek DB’ye yazılır
- Email ile kullanıcıya gönderilir

---

### 2) OTP Doğrula (Login)

```
POST /api/v1/auth/email/verify
```

Request:
```json
{
  "otpRef": "UUID",
  "otp": "123456"
}
```

Response:
```json
{
  "success": true,
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "user": {
      "id": "USER_UUID",
      "primaryEmail": "user@example.com"
    }
  }
}
```

---

## Google Login

Google login akışı mobil tarafta başlar.

1. Mobil uygulama Google Sign-In yapar
2. Google ID Token alınır
3. Backend’e gönderilir
4. Backend token’ı Google üzerinden doğrular

```
POST /api/v1/auth/google/login
```

Request:
```json
{
  "idToken": "GOOGLE_ID_TOKEN"
}
```

Response:
```json
{
  "success": true,
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "user": {
      "id": "USER_UUID",
      "primaryEmail": "user@gmail.com"
    }
  }
}
```

---

## Refresh Token Mekanizması

Refresh token sistemi **production güvenliği** için tasarlanmıştır.

Uygulanan kontroller:
1. Token Redis’te aktif mi?
2. Daha önce kullanılmış mı? (reuse/replay)
3. Rotation uygulanır mı?

Refresh endpoint:

```
POST /api/v1/auth/token/refresh
```

Request:
```json
{
  "refreshToken": "REFRESH_TOKEN"
}
```

Response:
```json
{
  "success": true,
  "data": {
    "accessToken": "NEW_ACCESS",
    "refreshToken": "NEW_REFRESH",
    "user": {
      "id": "USER_UUID",
      "primaryEmail": "user@example.com"
    }
  }
}
```

> Aynı refresh token tekrar kullanılırsa:
- `401 REFRESH_TOKEN_REUSED`

---

## Logout

### Tek Cihazdan Logout

Refresh token revoke edilir.

```
POST /api/v1/auth/logout
```

Request:
```json
{
  "refreshToken": "REFRESH_TOKEN"
}
```

Sonuç:
- Token Redis’ten silinir
- Aynı token ile refresh yapılamaz

---

## Logout All Devices

Kullanıcının tüm cihazlardaki refresh token’ları iptal edilir.

```
POST /api/v1/users/me/logout-all
Authorization: Bearer <accessToken>
```

Bu işlem:
- Kullanıcının Redis’teki tüm refresh token jti’larını siler
- Tüm cihazlardan logout sağlar

---

## Redis Yapısı

Redis’te tutulan anahtarlar:

- `babyvo:rt:active:{jti}` → aktif refresh token
- `babyvo:rt:used:{jti}` → kullanılmış token (replay koruması)
- `babyvo:rt:user:{userId}` → kullanıcının aktif token set’i

Bu yapı sayesinde:
- Multi-device desteklenir
- Logout all devices mümkün olur
- Replay attack engellenir

---

## Güvenlik Notları

- Refresh token rotation aktif
- Refresh token reuse engelli
- Stateless access token
- Redis atomic operasyonlar kullanılıyor

Öneriler:
- JWT_SECRET en az 64 karakter
- OTP_SECRET uzun ve random olmalı
- `.env` dosyaları repoya eklenmemeli

---

## cURL Örnekleri

### Email OTP Start
```bash
curl -X POST http://localhost:1905/api/v1/auth/email/start \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}'
```

### Email OTP Verify
```bash
curl -X POST http://localhost:1905/api/v1/auth/email/verify \
  -H "Content-Type: application/json" \
  -d '{"otpRef":"UUID","otp":"123456"}'
```

### Google Login
```bash
curl -X POST http://localhost:1905/api/v1/auth/google/login \
  -H "Content-Type: application/json" \
  -d '{"idToken":"GOOGLE_ID_TOKEN"}'
```

### Refresh Token
```bash
curl -X POST http://localhost:1905/api/v1/auth/token/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"REFRESH_TOKEN"}'
```

### Logout
```bash
curl -X POST http://localhost:1905/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"REFRESH_TOKEN"}'
```

---

## Konfigürasyon

Ortam değişkenleri ile yönetilir:

- DB_URL, DB_USER, DB_PASS
- REDIS_HOST, REDIS_PORT
- SMTP_USER, SMTP_PASS
- OTP_SECRET
- JWT_SECRET

---

## Yol Haritası

- Apple Sign-In
- Baby / Parent yetkilendirme
- Invite & permission sistemi
- Audit log ve security event’leri
