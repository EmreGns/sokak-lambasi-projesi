package com.example.sokaklambasi.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.sokaklambasi.R
import com.example.sokaklambasi.adapter.NotificationAdapter
import com.example.sokaklambasi.databinding.ActivityMainBinding
import com.example.sokaklambasi.model.Notification
import com.example.sokaklambasi.network.ApiClient
import com.example.sokaklambasi.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var apiService: ApiService
    private lateinit var notificationAdapter: NotificationAdapter
    private var isManualMode = false
    private var updateJob: Job? = null
    private var notificationId = 1
    private val CHANNEL_ID = "error_channel"
    
    // Sensör durumları için değişkenler
    private var lastPir1State: Boolean = false
    private var lastPir2State: Boolean = false
    private var lastMotionState: Boolean = false
    private var lastLdrState: Boolean = false
    private var lastSensorUpdateTime: Long = 0
    private var sensorErrorShown = false
    private var isFirstUpdate = true
    private var backendErrorShown = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Bildirim izni gerekli", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

        apiService = ApiClient.apiService
        createNotificationChannel()
        checkNotificationPermission()
        setupNotificationRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        startPeriodicUpdates()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hata Bildirimleri"
            val descriptionText = "Sokak lambası uygulaması hata bildirimleri"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showErrorNotification(errorMessage: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("Sokak Lambası Hatası")
            .setContentText(errorMessage)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(notificationId++, builder.build())
        }
    }

    private fun setupNotificationRecyclerView() {
        notificationAdapter = NotificationAdapter()
        binding.notificationRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = notificationAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchAllData()
        }
    }

    private fun setupClickListeners() {
        binding.btnToggleMode.setOnClickListener {
            toggleMode()
        }

        binding.btnLampOn.setOnClickListener {
            controlLamp(true)
        }

        binding.btnLampOff.setOnClickListener {
            controlLamp(false)
        }
    }

    private fun startPeriodicUpdates() {
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                fetchAllData()
                delay(800) // ESP32 ile aynı süre (0.8 saniye)
            }
        }
    }

    private fun checkSensorStatus(data: Map<String, Any>) {
        val currentTime = System.currentTimeMillis()
        val pir1State = data["pir1Detected"] as? Boolean ?: false
        val pir2State = data["pir2Detected"] as? Boolean ?: false
        val motionState = data["motionDetected"] as? Boolean ?: false
        val ldrState = data["isDark"] as? Boolean ?: false

        // İlk güncelleme
        if (isFirstUpdate) {
            lastPir1State = pir1State
            lastPir2State = pir2State
            lastMotionState = motionState
            lastLdrState = ldrState
            lastSensorUpdateTime = currentTime
            isFirstUpdate = false
            return
        }

        // Sensör değerleri değiştiyse zamanı güncelle
        val hasChanged = pir1State != lastPir1State || 
                        pir2State != lastPir2State || 
                        motionState != lastMotionState || 
                        ldrState != lastLdrState

        if (hasChanged) {
            lastPir1State = pir1State
            lastPir2State = pir2State
            lastMotionState = motionState
            lastLdrState = ldrState
            lastSensorUpdateTime = currentTime
            sensorErrorShown = false
            Log.d("SensorCheck", "Sensör değerleri değişti: PIR1=$pir1State, PIR2=$pir2State, Motion=$motionState, LDR=$ldrState")
        } else {
            // 5 saniye boyunca değerler değişmediyse ve hata gösterilmediyse bildirim göster
            if (currentTime - lastSensorUpdateTime > 30000 && !sensorErrorShown) {
                Log.d("SensorCheck", "Sensör değerleri 30 saniyedir değişmedi!")
                showErrorNotification("ESP32'den sensör verileri alınamıyor olabilir. Sensörler 30 saniyedir güncellenmedi.")
                sensorErrorShown = true
            }
        }
    }

    private fun fetchAllData() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getSensorData()
                }
                
                if (response["success"] as Boolean) {
                    val data = response["data"] as Map<String, Any>
                    checkSensorStatus(data)
                    updateUI(data)
                    backendErrorShown = false
                } else {
                    val errorMessage = "Veri alınamadı: ${response["message"]}"
                    if (!backendErrorShown) {
                        showErrorNotification(errorMessage)
                        backendErrorShown = true
                    }
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
                binding.swipeRefresh.isRefreshing = false
            } catch (e: Exception) {
                binding.swipeRefresh.isRefreshing = false
                val errorMessage = when {
                    e.message?.contains("failed to connect") == true -> 
                        "Backend bağlantısı kurulamadı. Backend servisi başlatılmamış olabilir."
                    else -> "Veri alınamadı: ${e.message}"
                }
                if (!backendErrorShown) {
                    showErrorNotification(errorMessage)
                    backendErrorShown = true
                }
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(data: Map<String, Any>) {
        binding.apply {
            // Sensör verileri
            textLdrValue.text = "Işık Durumu: ${if (data["isDark"] as Boolean) "Karanlık" else "Aydınlık"}"
            textMotion.text = "Hareket Durumu: ${if (data["motionDetected"] as Boolean) "Var" else "Yok"}"
            textMode.text = "Mod: ${if (data["isManualMode"] as Boolean) "Manuel" else "Otomatik"}"
            
            // Lamba durumu
            textLampStatus.text = "Lamba Durumu: ${if (data["lightsOn"] as Boolean) "Açık" else "Kapalı"}"
            
            // Manuel mod durumu
            isManualMode = data["isManualMode"] as Boolean
            updateManualControls()
        }
    }

    private fun toggleMode() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val newMode = !isManualMode
                val response = withContext(Dispatchers.IO) {
                    apiService.setManualMode(mapOf("isManualMode" to newMode))
                }
                
                if (response["success"] as Boolean) {
                    isManualMode = newMode
                    updateManualControls()
                    addNotification("Mod Değişikliği", if (isManualMode) "Manuel moda geçme komutu gönderildi" else "Otomatik moda geçme komutu gönderildi")
                } else {
                    Toast.makeText(this@MainActivity, "Mod değiştirilemedi", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Mod değiştirilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun controlLamp(turnOn: Boolean) {
        if (!isManualMode) {
            Toast.makeText(this, "Lütfen önce manuel moda geçin", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    if (turnOn) apiService.lampOn() else apiService.lampOff()
                }
                
                if (response["success"] as Boolean) {
                    fetchAllData() // UI'ı güncelle
                    addNotification("Lamba Kontrolü", if (turnOn) "Lamba yakma komutu gönderildi" else "Lamba söndürme komutu gönderildi")
                } else {
                    Toast.makeText(this@MainActivity, "Lamba kontrol edilemedi", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Lamba kontrol edilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateManualControls() {
        binding.manualControls.visibility = if (isManualMode) View.VISIBLE else View.GONE
        binding.btnToggleMode.text = if (isManualMode) "Otomatik Moda Geç" else "Manuel Moda Geç"
    }

    private fun addNotification(title: String, message: String) {
        val notification = Notification(title, message)
        notificationAdapter.addNotification(notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
    }
}
