#include <Arduino.h>
#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// Konfigürasyon sabitleri
namespace Config {
    // WiFi ayarları
    constexpr char WIFI_SSID[] = "Emre";
    constexpr char WIFI_PASSWORD[] = "emregunes";
    
    // Firebase ayarları
    constexpr char API_KEY[] = "AIzaSyD3IVKKRMNlhpcd4dP8-3aJ2buymnmR37Q";
    constexpr char DATABASE_URL[] = "https://sokak-lambasi-default-rtdb.europe-west1.firebasedatabase.app";
    
    // Pin tanımlamaları
    constexpr uint8_t LDR_PIN = 32;
    constexpr uint8_t PIR1_PIN = 33;
    constexpr uint8_t PIR2_PIN = 34;
    constexpr uint8_t RELE1_PIN = 27;
    constexpr uint8_t RELE2_PIN = 23;
    constexpr uint8_t RELE3_PIN = 25;
    constexpr uint8_t RELE4_PIN = 22;
    
    // Sistem parametreleri
    constexpr int LDR_THRESHOLD = 2000;
    constexpr unsigned long LIGHT_OFF_DELAY = 5000;
    constexpr unsigned long FIREBASE_UPDATE_INTERVAL = 800;  // 0.8 saniye
    constexpr unsigned long LOOP_DELAY = 100;
}

// Sistem durumları
class SystemState {
public:
    enum class Mode {
        AUTOMATIC,
        MANUAL
    };
    
    enum class AutoState {
        IDLE,
        MOTION_DETECTED
    };
    
    struct SensorData {
        int ldrValue;
        bool pir1;
        bool pir2;
        bool isDark;
        bool motionDetected;
    };
    
private:
    Mode currentMode = Mode::AUTOMATIC;
    AutoState autoState = AutoState::IDLE;
    bool lightsOn = false;
    unsigned long lastMotionTime = 0;
    unsigned long lastFirebaseUpdate = 0;
    
    FirebaseData fbdo;
    FirebaseAuth auth;
    FirebaseConfig config;
    bool firebaseInitialized = false;

public:
    // Getter/Setter metodları
    Mode getMode() const { return currentMode; }
    bool isLightsOn() const { return lightsOn; }
    
    // Firebase başlatma
    void initializeFirebase() {
        config.api_key = Config::API_KEY;
        config.database_url = Config::DATABASE_URL;
        
        if (Firebase.signUp(&config, &auth, "", "")) {
            Serial.println(F("Firebase bağlantısı başarılı"));
            firebaseInitialized = true;
        } else {
            Serial.println(F("Firebase bağlantısı başarısız"));
            return;
        }
        
        config.token_status_callback = tokenStatusCallback;
        Firebase.begin(&config, &auth);
        Firebase.reconnectWiFi(true);
    }
    
    // Sensör verilerini işle
    void processSensorData(const SensorData& sensors) {
        // Modu her döngüde Firebase'den kontrol et
        checkFirebaseControls();
        if (currentMode == Mode::AUTOMATIC) {
            handleAutoMode(sensors);
        } else {
            // Manuel modda sadece Firebase'deki lightsOn dikkate alınır
            // Sensörler tamamen devre dışı
        }
        updateFirebase(sensors);
    }

private:    void handleAutoMode(const SensorData& sensors) {
        switch (autoState) {
            case AutoState::IDLE:
                if (sensors.isDark && sensors.motionDetected) {
                    setLights(true);
                    lastMotionTime = millis();
                    autoState = AutoState::MOTION_DETECTED;
                    Serial.println(F("Hareket algılandı → Lambalar AÇIK"));
                    // Bildirim gönder
                    FirebaseJson notif;
                    notif.set("title", "Hareket Bildirimi");
                    notif.set("message", "Hareket algılandı, lambalar açıldı");
                    notif.set("timestamp", (int)time(nullptr));
                    Firebase.RTDB.pushJSON(&fbdo, "notifications", &notif);
                }
                break;
            case AutoState::MOTION_DETECTED:
                if (sensors.motionDetected) {
                    lastMotionTime = millis();
                } else if (millis() - lastMotionTime > Config::LIGHT_OFF_DELAY) {
                    setLights(false);
                    autoState = AutoState::IDLE;
                    Serial.println(F("Hareket bitti → Lambalar KAPALI"));
                    // Bildirim gönder
                    FirebaseJson notif;
                    notif.set("title", "Hareket Durumu");
                    notif.set("message", "Hareket kesildi, lambalar kapatıldı");
                    notif.set("timestamp", (int)time(nullptr));
                    Firebase.RTDB.pushJSON(&fbdo, "notifications", &notif);
                }
                break;
        }
    }
    void handleModeChange(Mode newMode) {
        currentMode = newMode;
        if (newMode == Mode::AUTOMATIC) {
            autoState = AutoState::IDLE;
            setLights(false);
        }
        Serial.print(F("Mod değişti: "));
        Serial.println(currentMode == Mode::AUTOMATIC ? F("OTOMATİK") : F("MANUEL"));
        // Sadece isManualMode anahtarını güncelle
        Firebase.RTDB.setBool(&fbdo, F("/veriler/isManualMode"), currentMode == Mode::MANUAL);
    }
      bool setLights(bool state) {
        digitalWrite(Config::RELE1_PIN, state ? HIGH : LOW);
        digitalWrite(Config::RELE2_PIN, state ? HIGH : LOW);
        digitalWrite(Config::RELE3_PIN, state ? HIGH : LOW);
        digitalWrite(Config::RELE4_PIN, state ? HIGH : LOW);
        
        // Lamba durumunu doğrula
        delay(100); // Rölelerin durumunun oturması için kısa bekle
        bool success = true;
        if (digitalRead(Config::RELE1_PIN) != (state ? HIGH : LOW) ||
            digitalRead(Config::RELE2_PIN) != (state ? HIGH : LOW) ||
            digitalRead(Config::RELE3_PIN) != (state ? HIGH : LOW) ||
            digitalRead(Config::RELE4_PIN) != (state ? HIGH : LOW)) {
            success = false;
        }
        
        if (!success) {
            String errorMsg = state ? 
                "Lamba açma işlemi başarısız oldu!" :
                "Lamba kapatma işlemi başarısız oldu!";
            
            // Hata bildirimi gönder
            FirebaseJson errorJson;
            errorJson.set("type", "LAMP_STATE");
            errorJson.set("message", errorMsg);
            errorJson.set("timestamp", (int)time(nullptr));
            errorJson.set("severity", "high");
            errorJson.set("component", "relay");
            errorJson.set("success", false);
            
            if (Firebase.RTDB.pushJSON(&fbdo, "errors", &errorJson)) {
                Serial.println(F("Hata bildirimi gönderildi"));
            } else {
                Serial.printf("Hata bildirimi gönderilemedi: %s\n", fbdo.errorReason().c_str());
            }
        }
        
        lightsOn = state;
        return success;
    }    void updateFirebase(const SensorData& sensors) {
        if (!firebaseInitialized) return;
        
        unsigned long now = millis();
        if (now - lastFirebaseUpdate < Config::FIREBASE_UPDATE_INTERVAL) return;
        
        FirebaseJson json;
        json.set("ldrValue", sensors.ldrValue);
        json.set("isDark", sensors.isDark);
        json.set("motionDetected", sensors.motionDetected);
        json.set("pir1", sensors.pir1);
        json.set("pir2", sensors.pir2);
        json.set("lightsOn", lightsOn);
        json.set("isManualMode", currentMode == Mode::MANUAL);
        
        // Firebase'e senkron veri gönderimi ve hata kontrolü
        if (Firebase.RTDB.set(&fbdo, "veriler", &json)) {
            lastFirebaseUpdate = now;
            Serial.println("Firebase güncelleme başarılı!");
            Serial.print("LDR Value: "); Serial.println(sensors.ldrValue);
            Serial.print("isDark: "); Serial.println(sensors.isDark);
            Serial.print("Motion: "); Serial.println(sensors.motionDetected);
            Serial.print("PIR1: "); Serial.println(sensors.pir1);
            Serial.print("PIR2: "); Serial.println(sensors.pir2);
        } else {
            Serial.print("Firebase güncelleme hatası: ");
            Serial.println(fbdo.errorReason());
            reportError("FIREBASE", fbdo.errorReason().c_str());
        }
    }

    void checkFirebaseControls() {
        if (!firebaseInitialized) return;
        
        if (Firebase.RTDB.getBool(&fbdo, "veriler/isManualMode")) {
            bool isManual = fbdo.to<bool>();
            if (isManual && currentMode == Mode::AUTOMATIC) {
                currentMode = Mode::MANUAL;
            } else if (!isManual && currentMode == Mode::MANUAL) {
                currentMode = Mode::AUTOMATIC;
            }
        }
        
        if (currentMode == Mode::MANUAL) {
            if (Firebase.RTDB.getBool(&fbdo, "veriler/lightsOn")) {
                bool shouldBeOn = fbdo.to<bool>();
                if (shouldBeOn != lightsOn) {
                    setLights(shouldBeOn);
                }
            }
        }
    }
    
    // Hata bildirimi gönderme
    void reportError(const char* errorType, const char* message) {
        if (!firebaseInitialized) return;
        
        FirebaseJson json;
        json.set("type", errorType);
        json.set("message", message);
        json.set("timestamp", (int)time(nullptr));
        
        if (Firebase.RTDB.pushAsync(&fbdo, "errors", &json)) {
            Serial.println(F("Hata bildirimi gönderildi"));
        } else {
            Serial.println(F("Hata bildirimi gönderilemedi"));
        }
    }
};

// Global sistem durumu
SystemState systemState;

void setup() {
    Serial.begin(115200);
    
    // Pin modlarını ayarla
    pinMode(Config::LDR_PIN, INPUT);
    pinMode(Config::PIR1_PIN, INPUT);
    pinMode(Config::PIR2_PIN, INPUT);
    pinMode(Config::RELE1_PIN, OUTPUT);
    pinMode(Config::RELE2_PIN, OUTPUT);
    pinMode(Config::RELE3_PIN, OUTPUT);
    pinMode(Config::RELE4_PIN, OUTPUT);
    
    // WiFi bağlantısı
    WiFi.begin(Config::WIFI_SSID, Config::WIFI_PASSWORD);
    Serial.print(F("WiFi bağlanıyor"));
    while (WiFi.status() != WL_CONNECTED) {
        Serial.print(".");
        delay(300);
    }
    Serial.println(F("\nWiFi bağlandı"));
    
    // Firebase başlat
    systemState.initializeFirebase();
}

void loop() {
    int ldr = analogRead(Config::LDR_PIN);
    bool pir1 = digitalRead(Config::PIR1_PIN);
    bool pir2 = digitalRead(Config::PIR2_PIN);

    // DEBUG: Sensör değerlerini seri porttan yazdır
    Serial.print("LDR: "); Serial.print(ldr);
    Serial.print(" PIR1: "); Serial.print(pir1);
    Serial.print(" PIR2: "); Serial.println(pir2);

    SystemState::SensorData sensors{
        ldr,
        pir1,
        pir2,
        ldr >= Config::LDR_THRESHOLD,
        pir1 || pir2
    };
    systemState.processSensorData(sensors);
    // WiFi bağlantı kontrolü
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println(F("WiFi bağlantısı koptu. Yeniden bağlanılıyor..."));
        WiFi.begin(Config::WIFI_SSID, Config::WIFI_PASSWORD);
    }
    delay(Config::LOOP_DELAY);
}
