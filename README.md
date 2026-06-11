# 💡 Akıllı Sokak Lambası Projesi

Bu proje, özellikle kırsal kesimlerde ve az kullanılan yollarda **yüksek enerji tasarrufu** sağlamak amacıyla geliştirilmiş, ESP32 tabanlı ve bulut entegrasyonlu akıllı bir sokak lambası sistemidir.

---

## 📌 Proje Mantığı ve Çalışma Senaryosu

Sistem temel olarak iki farklı modda çalışır ve her iki modda da anlık olarak bulut üzerinden kontrol edilebilir:

- 🔄 **Otomatik Mod (Akıllı Tasarruf):** Sistem sadece **gece olduğunda (LDR sensörü ile)** ve **bir hareket algılandığında (PIR sensörü ile)** lambayı tam güçte yakar. Hareket olmadığı durumlarda lambayı kapatarak veya kısarak ciddi oranda enerji tasarrufu sağlar.
- 📱 **Manuel Mod (Uzaktan Kontrol):** Kullanıcı, mesafe sınırı olmaksızın **Android uygulama üzerinden** tek bir dokunuşla buluta (Firebase) bağlanarak lambaları istediği gibi açıp kapatabilir veya modlar arasında geçiş yapabilir.

---

## 🗺️ Algoritma ve Çalışma Akış Şeması

Sistemin ana kontrol lojiği ve karar mekanizmasının çalışma akış diyagramı:

<p align="center">
  <img width="650" alt="Sistem Çalışma Akış Şeması" src= <img width="702" height="234" alt="akış şeması" src="https://github.com/user-attachments/assets/73a6f7e7-a600-420b-b3b1-d70e971e7800" />

</p>

---

## ✨ Özellikler

- 🌙 **Gece ve Hareket Algılama:** Sadece ihtiyaç anında çalışan akıllı aydınlatma algoritması.
- 📉 **Kırsal Bölge Optimizasyonu:** Gereksiz gece aydınlatmasını önleyerek yüksek enerji tasarrufu.
- ☁️ **Bulut Entegrasyonu:** Firebase Realtime Database ile anlık ve gecikmesiz veri iletişimi.
- 📱 **Kullanıcı Dostu Android Arayüzü:** Lambaları uzaktan açma/kapama ve mod değiştirme butonları.
- 📊 **Sensör Analizi:** Ortam ışığı ve hareket geçmişinin uygulama içinden grafiksel takibi.
- 🔔 **Anlık Bildirimler:** Lamba durumu veya çalışma modu değiştiğinde Firebase Cloud Messaging (FCM) üzerinden telefona gelen push bildirimleri.

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
   git clone [https://github.com/EmreGns/sokak-lambasi-projesi.git](https://github.com/EmreGns/sokak-lambasi-projesi.git)
