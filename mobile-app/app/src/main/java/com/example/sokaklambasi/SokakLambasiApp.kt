package com.example.sokaklambasi

import android.app.Application
import com.google.firebase.FirebaseApp

class SokakLambasiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
