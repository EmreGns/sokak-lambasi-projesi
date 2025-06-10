package com.example.sokaklambasi.model

data class Notification(
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
) 