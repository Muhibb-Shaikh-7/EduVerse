package com.example.eduverse

import android.app.Application
import android.util.Log

/**
 * EduVerse Application Class
 *
 * This will be used to initialize:
 * - Firebase (when you add google-services.json)
 * - Hilt/Dagger (for dependency injection)
 * - Any other app-level initialization
 *
 * Currently: Empty and ready for Firebase setup
 */
class EduVerseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "EduVerse Application Started")

        // TODO: Initialize Firebase when ready
        // FirebaseApp.initializeApp(this)

        // TODO: Initialize other services
        // - Authentication
        // - Analytics
        // - Crash reporting
    }

    companion object {
        private const val TAG = "EduVerseApp"
    }
}
