# 💡 Akıllı Sokak Lambası Projesi

Bu proje, özellikle kırsal kesimlerde ve az kullanılan yollarda **yüksek enerji tasarrufu** sağlamak amacıyla geliştirilmiş, ESP32 tabanlı ve bulut entegrasyonlu akıllı bir sokak lambası sistemidir.

---

## 📌 Proje Mantığı ve Çalışma Senaryosu

Sistem temel olarak iki farklı modda çalışır ve her iki modda da anlık olarak bulut üzerinden kontrol edilebilir:

- 🔄 **Otomatik Mod (Akıllı Tasarruf):** Sistem sadece **gece olduğunda (LDR sensörü ile)** ve **bir hareket algılandığında (PIR sensörü ile)** lambayı tam güçte yakar. Hareket olmadığında ise lambayı tamamen kapatır.
- 📱 **Manuel Mod (Uzaktan Kontrol):** Kullanıcı, mesafe sınırı olmaksızın **Android uygulama üzerinden** tek bir dokunuşla buluta (Firebase) bağlanarak lambaları istediği gibi açıp kapatabilir.

---

## ✨ Özellikler

- 🌙 **Gece ve Hareket Algılama:** Sadece ihtiyaç anında çalışan akıllı aydınlatma algoritması.
- 📉 **Kırsal Bölge Optimizasyonu:** Gereksiz gece aydınlatmasını önleyerek yüksek enerji tasarrufu.
- ☁️ **Bulut Entegrasyonu:** Firebase Realtime Database ile anlık ve gecikmesiz veri iletişimi.
- 📱 **Kullanıcı Dostu Android Arayüzü:** Lambaları uzaktan açma/kapama ve mod değiştirme butonları.
- 🔔 **Anlık Bildirimler:** Lamba durumu veya çalışma modu değiştiğinde telefona gelen push bildirimleri.
- 📊 **Sensör Analizi:** Ortam ışığı ve hareket geçmişinin uygulama içinden grafiksel takibi.

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
| <img width="400" alt="Android Uygulama İkonu" src="https://github.com/user-attachments/assets/1739bad6-303b-4f4a-98fc-32f7fd4701ce" /> | <img width="400" alt="Uygulama İçi Kontrol Ekranı" src="https://github.com/user-attachments/assets/1739bad6-303b-4f4a-98fc-32f7fd4701ce" /> |

---

### 🛠️ Donanım Kurulumu & Fiziksel Prototip
Sistemin ESP32 tabanlı fiziksel prototip tasarımı ve donanım bileşenleri:

<p align="center">
  <img width="830" alt="Fiziksel Prototip Tasarımı" src="https://github.com/user-attachments/assets/fc46fcd4-8939-4ecf-bdd7-78163f046e63" />
</p>
