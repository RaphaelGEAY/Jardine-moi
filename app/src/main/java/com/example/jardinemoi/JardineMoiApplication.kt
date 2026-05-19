package com.example.jardinemoi

import android.app.Application
import com.google.firebase.FirebaseApp

class JardineMoiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
