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

## 👶 Baby & Ebeveyn Paylaşımı (Invite)

BabyVo, bir bebek profilinin birden fazla ebeveyn tarafından yönetilebilmesini destekler.
Bu bölüm, bebek oluşturma, ebeveyn davet etme ve davet kabul / reddetme akışlarını açıklar.

---

### 🧱 Temel Kavramlar

#### Baby
Bir bebek profilini temsil eder.

#### BabyParent
Bir kullanıcının bir bebek üzerindeki rolünü ve yetkilerini temsil eden ilişki tablosudur.

**Role (`BabyParentRole`)**
- OWNER → Bebeği oluşturan kişi (tek)
- CO_PARENT → Anne / Baba
- CAREGIVER → Bakıcı
- VIEWER → Sadece görüntüleme

**Permission (`BabyPermission`)**
- READ_ONLY
- READ_WRITE

> Role = kim, Permission = ne yapabilir

---

### 🔐 Güvenlik Notları

- Tüm endpoint’ler JWT access token ister
- Invite token’ları client’a plain döner, DB’de hash’lenmiş saklanır
- Invite token’ları süreli ve tek kullanımlıktır
- Email davetlerinde email eşleşmesi zorunludur

---

## 🚼 Baby API’leri

### Bebek Oluşturma

POST /api/v1/babies

```bash
curl -X POST http://localhost:1905/api/v1/babies \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Deniz",
    "birthDate": "2025-04-03",
    "sex": "MALE"
  }'
```

---

### Kullanıcının Bebeklerini Listeleme

GET /api/v1/babies

```bash
curl -X GET http://localhost:1905/api/v1/babies \
  -H "Authorization: Bearer ACCESS_TOKEN"
```

---

### Bebek Detayı

GET /api/v1/babies/{babyId}

```bash
curl -X GET http://localhost:1905/api/v1/babies/{babyId} \
  -H "Authorization: Bearer ACCESS_TOKEN"
```

---

### Bebek Güncelleme

PATCH /api/v1/babies/{babyId}

```bash
curl -X PATCH http://localhost:1905/api/v1/babies/{babyId} \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "birthDate": "2025-04-03"
  }'
```

---

## 🤝 Invite API’leri

### Davet Oluşturma

POST /api/v1/babies/{babyId}/invites

```bash
curl -X POST http://localhost:1905/api/v1/babies/{babyId}/invites \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "anne@example.com",
    "permission": "READ_WRITE"
  }'
```

---

### Daveti Kabul Etme

POST /api/v1/invites/accept

```bash
curl -X POST http://localhost:1905/api/v1/invites/accept \
  -H "Authorization: Bearer INVITED_USER_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "inviteToken": "TOKEN_VALUE"
  }'
```

---

### Daveti Reddetme

POST /api/v1/invites/reject

```bash
curl -X POST http://localhost:1905/api/v1/invites/reject \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "inviteToken": "TOKEN_VALUE"
  }'
```

---

