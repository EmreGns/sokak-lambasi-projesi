# 💡 Akıllı Sokak Lambası Projesi

Bu proje, **ESP32 tabanlı akıllı sokak lambası** sisteminin Android uygulaması ve backend servisini içerir. Sistem, ortam ışığı ve hareket sensörlerini kullanarak lambaları otomatik olarak kontrol eder veya manuel müdahaleye izin verir.

---

## ✨ Özellikler

- 🔄 Otomatik / Manuel mod geçişi
- 📊 Sensör verilerinin gerçek zamanlı görüntülenmesi
- 🔔 Durum değişikliklerinde anlık bildirimler
- 📱 Kullanıcı dostu Android arayüzü
- 🔒 Güvenli veri iletişimi
- 📈 Sensör verilerinin grafiksel analizi
- 🌐 REST API ile uzaktan kontrol
- 🔥 Firebase Realtime Database entegrasyonu

---

## 🏗️ Sistem Mimarisi

Sistem üç ana bileşenden oluşur:

| Katman | Bileşen | Görev |
|--------|---------|-------|
| Donanım | ESP32 | Sensör verilerini toplar, lambayı kontrol eder |
| Mobil | Android Uygulaması | Kullanıcı arayüzü ve bildirimleri yönetir |
| Sunucu | Backend Servisi | Firebase ile iletişimi sağlar, API sunar |

---

## 📦 Gereksinimler

- Android Studio Arctic Fox veya üzeri
- Node.js 14.x veya üzeri
- Firebase hesabı
- ESP32 geliştirme kartı
- LDR ve PIR sensörleri

---

## ⚙️ Kurulum

### Android Uygulaması

1. Projeyi klonlayın:
   ```bash
   git clone https://github.com/EmreGns/sokak-lambasi-projesi.git
   ```
2. Android Studio'da projeyi açın.
3. `google-services.json` dosyasını `app/` dizinine ekleyin.
4. Gerekli bağımlılıkları yükleyin:
   ```bash
   ./gradlew build
   ```
5. Uygulamayı derleyin ve çalıştırın.

### Backend Servisi

1. Backend dizinine gidin:
   ```bash
   cd sokak-lambasi-projesi/backend
   ```
2. Bağımlılıkları yükleyin:
   ```bash
   npm install
   ```
3. `.env` dosyasını oluşturun ve aşağıdaki değişkenleri ekleyin:
   ```env
   FIREBASE_API_KEY=your_api_key
   FIREBASE_AUTH_DOMAIN=your_auth_domain
   FIREBASE_PROJECT_ID=your_project_id
   FIREBASE_STORAGE_BUCKET=your_storage_bucket
   FIREBASE_MESSAGING_SENDER_ID=your_sender_id
   FIREBASE_APP_ID=your_app_id
   FIREBASE_DB_URL=https://<proje-adı>.firebaseio.com
   ```
4. `firebase-admin.json` dosyasını backend klasörüne ekleyin.
5. Servisi başlatın:
   ```bash
   npm start
   ```

---

## 🔌 API Endpoint'leri

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| `POST` | `/lights/on` | Lambayı yakar |
| `POST` | `/lights/off` | Lambayı kapatır |
| `GET` | `/status` | Işık ve sensör verilerini döner |

---

## 📱 Kullanım

1. Uygulamayı açın.
2. **Otomatik modda:** Sistem ortam ışığı ve hareket sensörlerine göre lambayı otomatik kontrol eder.
3. **Manuel modda:** "Lambayı Yak" ve "Lambayı Söndür" butonlarıyla lambayı kontrol edebilirsiniz.
4. **Bildirimler** şu durumlarda gelir:
   - Lamba durumu değiştiğinde
   - Mod değiştiğinde
   - Sensör verisi alınamadığında
   - Backend bağlantısı kesildiğinde

## 📸 Ekran Görüntüleri ve Şemalar

### 📱 Mobil Uygulama Arayüzü & İkon Tasarımı
Uygulamanın kullanıcı arayüzü ve mobil cihazlardaki görünümü:

| Android Uygulama İkonu | Mobil Kontrol Paneli |
|------------------------|----------------------|
| <img width="400" alt="Android Uygulama İkonu" src="https://github.com/user-attachments/assets/1739bad6-303b-4f4a-98fc-32f7fd4701ce" /> | <img width="400" alt="Uygulama İçi Kontrol Ekranı" src="https://github.com/user-attachments/assets/e80ff74b-7e54-42fa-a8e6-8831f6a89196" /> |

---

### 🛠️ Donanım Kurulumu & Devre Tasarımı
Sistemin ESP32 tabanlı fiziksel prototipi ve devre şeması tasarımı:

| Donanım Prototip Tasarımı | Devre Şeması / Bağlantı Detayları |
|---------------------------|-----------------------------------|
| <img width="400" alt="Fiziksel Prototip Tasarımı" src="https://github.com/user-attachments/assets/fc46fcd4-8939-4ecf-bdd7-78163f046e63" /> | <img width="400" alt="Devre Şeması Bağlantıları" src="https://github.com/user-attachments/assets/2283bef0-f17e-4f9b-a079-5bbd3be9feab" /> |
