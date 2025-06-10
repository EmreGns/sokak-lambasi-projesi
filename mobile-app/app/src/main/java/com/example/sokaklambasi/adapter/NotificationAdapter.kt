package com.example.sokaklambasi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sokaklambasi.R
import com.example.sokaklambasi.model.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    private val notifications = mutableListOf<Notification>()
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun addNotification(notification: Notification) {
        notifications.add(0, notification) // Yeni bildirimi en üste ekle
        if (notifications.size > 10) { // Son 10 bildirimi tut
            notifications.removeAt(notifications.size - 1)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount() = notifications.size

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.notificationIcon)
        private val title: TextView = itemView.findViewById(R.id.notificationTitle)
        private val message: TextView = itemView.findViewById(R.id.notificationMessage)
        private val time: TextView = itemView.findViewById(R.id.notificationTime)

        fun bind(notification: Notification) {
            title.text = notification.title
            message.text = notification.message
            time.text = dateFormat.format(Date(notification.timestamp))

            // Bildirim türüne göre ikon seç
            icon.setImageResource(
                when {
                    notification.title.contains("Mod") -> R.drawable.ic_info
                    notification.title.contains("Lamba") -> R.drawable.ic_info
                    else -> R.drawable.ic_info
                }
            )
        }
    }
} 