package com.example.sokaklambasi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.sokaklambasi.network.ApiClient
import com.example.sokaklambasi.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        registerToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        message.notification?.let { notification ->
            showNotification(notification.title ?: "Bildirim", notification.body ?: "")
        }

        message.data.isNotEmpty().let {
            val title = message.data["title"] ?: "Bildirim"
            val body = message.data["body"] ?: ""
            showNotification(title, body)
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "sokak_lambasi_channel"
        val channelName = "Sokak Lambası Bildirimleri"
        val channelDescription = "Sokak lambası durumu ve sensör bildirimleri"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = channelDescription
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun registerToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiClient.apiService.registerToken(mapOf("token" to token))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
