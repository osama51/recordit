package com.toddler.recordit

import android.app.Application
import android.net.ConnectivityManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MyApplication: Application() {
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var storage: FirebaseStorage
    lateinit var database: FirebaseDatabase

    override fun onCreate() {
        super.onCreate()
        // Initialize Hilts

        // Initialize Timber

        // Initialize Firebase
        val firebaseApp = FirebaseApp.initializeApp(this)

        firebaseAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = Firebase.database
    }

    // fun to check for internet connection
    fun checkInternetConnection(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}