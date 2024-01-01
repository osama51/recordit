package com.toddler.recordit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Hilts

        // Initialize Timber
    }
}