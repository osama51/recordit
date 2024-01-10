package com.toddler.recordit

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.toddler.recordit.authentication.FirebaseAuthentication
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MyApplication: Application() {
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()
        // Initialize Hilts

        // Initialize Timber

        // Initialize Firebase
        val firebaseApp = FirebaseApp.initializeApp(this)

        firebaseAuth = FirebaseAuth.getInstance()
    }
}