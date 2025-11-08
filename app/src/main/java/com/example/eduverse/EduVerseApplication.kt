package com.example.eduverse

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * EduVerse Application Class with Hilt
 *
 * This initializes:
 * - Firebase
 * - Hilt/Dagger for dependency injection
 * - Any other app-level initialization
 */
@HiltAndroidApp
class EduVerseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "EduVerse Application Started")

        // Firebase is automatically initialized via google-services plugin
        // Hilt is initialized via @HiltAndroidApp annotation
    }

    companion object {
        private const val TAG = "EduVerseApp"
    }
}
