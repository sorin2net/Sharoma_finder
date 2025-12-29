package com.example.sharoma_finder

import android.app.Application
import android.util.Log
import com.google.firebase.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * ✅ Clasa Application - Prima componentă care se inițializează
 *
 * CÂND SE RULEAZĂ: Când aplicația pornește, ÎNAINTE de orice Activity
 * DE CE: Firebase Crashlytics trebuie inițializat GLOBAL, nu în fiecare Activity
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d("MyApplication", "🚀 App starting - Initializing Firebase")

        // 1. Inițializare Firebase Core
        try {
            FirebaseApp.initializeApp(this)
            Log.d("MyApplication", "✅ Firebase initialized")
        } catch (e: Exception) {
            Log.e("MyApplication", "❌ Firebase init failed: ${e.message}")
        }

        // 2. ✅ CONFIGURARE CRASHLYTICS CU FALLBACK (Soluția propusă)
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()

            crashlytics.apply {
                setCrashlyticsCollectionEnabled(true) // Activează raportarea

                // Identificator generic la pornire
                setUserId("anonymous_user")

                // Adaugă chei personalizate pentru context
                setCustomKey("app_version", BuildConfig.VERSION_NAME)
                setCustomKey("debug_mode", BuildConfig.DEBUG)
            }
            Log.d("MyApplication", "✅ Crashlytics configured successfully")

        } catch (e: Exception) {
            // Dacă setup-ul Crashlytics eșuează, logăm eroarea local
            Log.e("MyApplication", "❌ Crashlytics setup failed: ${e.message}")
            // Aici poți implementa, în viitor, o salvare a log-urilor într-un fișier local
        }
    }
}