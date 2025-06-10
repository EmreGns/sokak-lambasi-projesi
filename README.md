# Akıllı Sokak Lambası Projesi

Bu proje, ESP32 tabanlı akıllı sokak lambası sisteminin Android uygulaması ve backend servisini içerir. Sistem, ortam ışığı ve hareket sensörlerini kullanarak lambaları otomatik olarak kontrol eder veya manuel müdahaleye izin verir.

## Özellikler

- 🔄 Otomatik/Manuel mod geçişi
- 📊 Sensör verilerinin gerçek zamanlı görüntülenmesi
- 🔔 Durum değişikliklerinde bildirimler
- 📱 Kullanıcı dostu arayüz
- 🔒 Güvenli veri iletişimi
- 📈 Sensör verilerinin grafiksel analizi
- 🌐 REST API ile uzaktan kontrol
- 🔥 Firebase Realtime Database entegrasyonu

## Kurulum

### Gereksinimler

- Android Studio Arctic Fox veya üzeri
- Node.js 14.x veya üzeri
- Firebase hesabı
- ESP32 geliştirme kartı
- LDR ve PIR sensörleri

### Android Uygulaması

1. Projeyi klonlayın:
   ```bash
   git clone https://github.com/kullanici/sokak-lambasi-projesi.git
   ```

2. Android Studio'da projeyi açın

3. `google-services.json` dosyasını `app/` dizinine ekleyin

4. Gerekli bağımlılıkları yükleyin:
   ```bash
   ./gradlew build
   ```

5. Uygulamayı derleyin ve çalıştırın

### Backend Servisi

1. Backend dizinine gidin:
   ```bash
   cd sokak-lambasi-projesi/backend
   ```

2. Bağımlılıkları yükleyin:
   ```bash
   npm install
   ```

3. `.env` dosyasını oluşturun ve gerekli değişkenleri ekleyin:
   ```
   FIREBASE_API_KEY=your_api_key
   FIREBASE_AUTH_DOMAIN=your_auth_domain
   FIREBASE_PROJECT_ID=your_project_id
   FIREBASE_STORAGE_BUCKET=your_storage_bucket
   FIREBASE_MESSAGING_SENDER_ID=your_sender_id
   FIREBASE_APP_ID=your_app_id
   FIREBASE_DB_URL=https://<proje-adı>.firebaseio.com
   ```

4. `firebase-admin.json` dosyasını backend klasörüne ekleyin

5. Servisi başlatın:
   ```bash
   npm start
   ```

## API Endpoint'leri

Backend servisi aşağıdaki REST API endpoint'lerini sunar:

- `POST /lights/on`  → Lambayı yakar
- `POST /lights/off` → Lambayı kapatır
- `GET /status`      → Işık ve sensör verilerini döner

## Kullanım

1. Uygulamayı açın
2. Otomatik modda:
   - Sistem ortam ışığı ve hareket sensörlerine göre lambayı otomatik kontrol eder
3. Manuel modda:
   - "Lambayı Yak" ve "Lambayı Söndür" butonlarıyla lambayı kontrol edebilirsiniz
4. Bildirimler:
   - Lamba durumu değiştiğinde
   - Mod değiştiğinde
   - Sensör verisi alınamadığında
   - Backend bağlantısı kesildiğinde

## Sistem Mimarisi

Sistem üç ana bileşenden oluşur:
1. ESP32 (Donanım) - Sensör verilerini toplar ve lambayı kontrol eder
2. Android Uygulaması - Kullanıcı arayüzü ve bildirimleri yönetir
3. Backend Servisi - Firebase ile iletişimi sağlar ve API sunar

Detaylı mimari bilgisi için [docs/mimari.md](docs/mimari.md) dosyasına bakabilirsiniz.

## Katkıda Bulunma

1. Bu depoyu fork edin
2. Yeni bir özellik dalı oluşturun (`git checkout -b yeni-ozellik`)
3. Değişikliklerinizi commit edin (`git commit -am 'Yeni özellik: X'`)
4. Dalınıza push yapın (`git push origin yeni-ozellik`)
5. Bir Pull Request oluşturun

## Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakın.

## İletişim

- Proje Yöneticisi: [İsim Soyisim](mailto:email@example.com)
- GitHub: [@kullanici](https://github.com/kullanici)
