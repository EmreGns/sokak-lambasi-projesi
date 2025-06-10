# Mimari

Bu sistem 3 temel bileşenden oluşur:

1. **ESP32 (Donanım):**  
   - LDR ve PIR sensörlerinden veri alır.
   - Firebase Realtime Database'e veri gönderir:
     - `ldrValue`: Ortam ışık seviyesi (0-4095 arası)
     - `motionDetected`: Hareket algılama durumu (true/false)
     - `lightsOn`: Lamba durumu (true/false)
     - `isManualMode`: Manuel/Otomatik mod durumu (true/false)
   - Firebase'den gelen komutları dinler ve lambayı kontrol eder.

2. **Android Uygulama:**  
   - Firebase Realtime Database'den veri okur ve UI'ı günceller
   - Kullanıcının FCM token'ını Firebase'e kayıt eder
   - Bildirimleri alır ve gösterir:
     - Lamba kontrol bildirimleri
     - Mod değişikliği bildirimleri
     - Sensör hata bildirimleri
     - Backend bağlantı hata bildirimleri
   - Manuel modda lamba kontrolü sağlar
   - Otomatik/Manuel mod geçişi yapabilir
   - Sensör verilerini grafiklerle görselleştirir

3. **Node.js Backend:**  
   - Firebase Realtime Database'i dinler
   - Veri değişikliklerinde ilgili işlemleri yapar:
     - `lightsOn` değiştiğinde bildirim gönderir
     - `isManualMode` değiştiğinde bildirim gönderir
   - REST API sunar:
     - Sensör verilerini okuma
     - Lamba kontrolü
     - Mod değişikliği
   - Hata durumlarını yönetir ve bildirim gönderir

## Veri Akışı

1. **Sensör Verileri:**
   ```
   ESP32 -> Firebase Realtime DB -> Android App
   ```

2. **Lamba Kontrolü:**
   ```
   Android App -> Backend API -> Firebase -> ESP32
   ```

3. **Bildirimler:**
   ```
   Backend -> Firebase Cloud Messaging -> Android App
   ```

## Güvenlik

- Firebase Authentication ile kullanıcı doğrulaması
- API isteklerinde token kontrolü
- Güvenli veri iletişimi (HTTPS)
- Hassas verilerin şifrelenmesi

## Hata Yönetimi

- Sensör verisi alınamadığında bildirim
- Backend bağlantı hatası durumunda bildirim
- Firebase bağlantı hatası durumunda bildirim
- Manuel müdahale gerektiren durumlarda bildirim