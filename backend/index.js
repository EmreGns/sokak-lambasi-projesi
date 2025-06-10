// Gerekli modüller
require('dotenv').config();
const express = require('express');
const cors = require('cors');
const admin = require('firebase-admin');
const { getDatabase } = require('firebase-admin/database');
const serviceAccount = require('./firebase-admin.json');

// Firebase Admin başlat
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: process.env.FIREBASE_DB_URL
});

const db = getDatabase();
const app = express();

// Middleware
app.use(cors({
    origin: '*'  // Tüm kaynaklardan gelen isteklere izin ver
}));
app.use(express.json());

// Hata yönetimi middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ 
    success: false, 
    error: 'Sunucu hatası oluştu' 
  });
});

// Son bildirim zamanını takip etmek için
let lastNotificationTime = 0;
const NOTIFICATION_COOLDOWN = 5000; // 5 saniye bekleme süresi
let isNotificationInProgress = false; // Bildirim gönderimi devam ediyor mu?

// Bildirim gönderme yardımcı fonksiyonu
async function sendNotificationToAll(title, body) {
  try {
    const tokensSnapshot = await db.ref('tokens').once('value');
    const tokensObj = tokensSnapshot.val() || {};
    const tokens = Object.values(tokensObj);
    
    if (tokens.length === 0) {
      console.log('Bildirim gönderilemedi: Kayıtlı token yok');
      return;
    }

    const message = {
      notification: { 
        title, 
        body,
        sound: 'default'
      },
      tokens: tokens
    };

    const response = await admin.messaging().sendMulticast(message);
    console.log('Bildirim gönderildi:', response);
  } catch (err) {
    console.error('Bildirim gönderme hatası:', err);
  }
}

// Lambayı yak endpoint'i
app.post('/lights/on', async (req, res) => {
  try {
    console.log('Lamba yakma isteği alındı');
    await db.ref('veriler/lightsOn').set(true);
    console.log('Firebase güncellendi: lightsOn = true');
    
    // Bildirim gönder
    await sendNotificationToAll(
      'Lamba Durumu',
      'Hareket algılandı! Lamba yakıldı.'
    );

    res.json({ 
      success: true, 
      message: 'Lamba yakıldı.',
      timestamp: new Date().toISOString()
    });
  } catch (err) {
    console.error('Lamba yakma hatası:', err);
    res.status(500).json({ 
      success: false, 
      error: err.message 
    });
  }
});

// Lambayı söndür endpoint'i
app.post('/lights/off', async (req, res) => {
  try {
    console.log('Lamba kapatma isteği alındı');
    await db.ref('veriler/lightsOn').set(false);
    console.log('Firebase güncellendi: lightsOn = false');
    
    // Bildirim gönder
    await sendNotificationToAll(
      'Lamba Durumu',
      'Hareket yok! Lamba söndürüldü.'
    );

    res.json({ 
      success: true, 
      message: 'Lamba kapatıldı.',
      timestamp: new Date().toISOString()
    });
  } catch (err) {
    console.error('Lamba kapatma hatası:', err);
    res.status(500).json({ 
      success: false, 
      error: err.message 
    });
  }
});

// Işık ve sensör durumunu sorgulayan endpoint
app.get('/status', async (req, res) => {
  try {
    const snapshot = await db.ref('veriler').once('value');
    res.json({ 
      success: true, 
      data: snapshot.val(),
      timestamp: new Date().toISOString()
    });
  } catch (err) {
    res.status(500).json({ 
      success: false, 
      error: err.message 
    });
  }
});

// Manuel/Otomatik mod değiştirme endpoint'i
app.post('/lights/mode', async (req, res) => {
  try {
    const { isManualMode } = req.body;
    console.log('Mod değiştirme isteği:', isManualMode);
    await db.ref('veriler/isManualMode').set(isManualMode);
    console.log('Firebase güncellendi: isManualMode =', isManualMode);
    
    // Bildirim gönder
    await sendNotificationToAll(
      'Mod Değişikliği',
      `Lamba modu ${isManualMode ? 'manuel' : 'otomatik'} olarak değiştirildi.`
    );

    res.json({ 
      success: true, 
      message: `Mod ${isManualMode ? 'manuel' : 'otomatik'} olarak değiştirildi.`,
      timestamp: new Date().toISOString()
    });
  } catch (err) {
    console.error('Mod değiştirme hatası:', err);
    res.status(500).json({ 
      success: false, 
      error: err.message 
    });
  }
});

// FCM token kaydetme endpoint'i
app.post('/register-token', async (req, res) => {
  const { token } = req.body;
  if (!token) {
    return res.status(400).json({ success: false, error: 'Token gerekli' });
  }
  try {
    await db.ref('tokens').push(token);
    res.json({ success: true, message: 'Token kaydedildi' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// Push notification gönderme endpoint'i
app.post('/send-notification', async (req, res) => {
  const { title, body } = req.body;
  if (!title || !body) {
    return res.status(400).json({ success: false, error: 'Başlık ve mesaj gerekli' });
  }
  try {
    const tokensSnapshot = await db.ref('tokens').once('value');
    const tokensObj = tokensSnapshot.val() || {};
    const tokens = Object.values(tokensObj);
    if (tokens.length === 0) {
      return res.status(400).json({ success: false, error: 'Kayıtlı token yok' });
    }
    const message = {
      notification: { title, body },
      tokens: tokens
    };
    const response = await admin.messaging().sendMulticast(message);
    res.json({ success: true, message: 'Bildirim gönderildi', response });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ 
    success: false, 
    error: 'Endpoint bulunamadı' 
  });
});

// Sunucuyu başlat
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`API sunucusu ${PORT} portunda çalışıyor.`);
});
