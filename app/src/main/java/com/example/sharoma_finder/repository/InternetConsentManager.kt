package com.example.sharoma_finder.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.core.content.edit

/**
 * ✅ MANAGER PENTRU CONSIMȚĂMÂNT INTERNET
 *
 * Gestionează:
 * - Salvarea acceptului utilizatorului pentru utilizarea internetului
 * - Verificarea conexiunii la internet
 * - Starea de consimțământ (pentru a bloca funcționalitatea Firebase/Maps)
 */
class InternetConsentManager(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences(
        "internet_consent_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_CONSENT_GIVEN = "internet_consent_given"
        private const val KEY_CONSENT_TIMESTAMP = "internet_consent_timestamp"
        private const val KEY_CONSENT_ASKED = "internet_consent_asked"
    }

    /**
     * ✅ Verifică dacă utilizatorul a acordat consimțământul pentru internet
     */
    fun hasInternetConsent(): Boolean {
        val consent = sharedPreferences.getBoolean(KEY_CONSENT_GIVEN, false)
        Log.d("InternetConsent", "Checking consent: $consent")
        return consent
    }

    /**
     * ✅ Verifică dacă am cerut deja consimțământul (pentru a nu deranja utilizatorul mereu)
     */
    fun hasAskedForConsent(): Boolean {
        return sharedPreferences.getBoolean(KEY_CONSENT_ASKED, false)
    }

    /**
     * ✅ Salvează că am cerut consimțământul (chiar dacă utilizatorul a refuzat)
     */
    fun markConsentAsked() {
        sharedPreferences.edit {
            putBoolean(KEY_CONSENT_ASKED, true)
        }
        Log.d("InternetConsent", "Marked consent as asked")
    }

    /**
     * ✅ Salvează acordul utilizatorului
     */
    fun grantConsent() {
        sharedPreferences.edit {
            putBoolean(KEY_CONSENT_GIVEN, true)
            putLong(KEY_CONSENT_TIMESTAMP, System.currentTimeMillis())
            putBoolean(KEY_CONSENT_ASKED, true)
        }
        Log.d("InternetConsent", "✅ Internet consent GRANTED")
    }

    /**
     * ✅ Revocă acordul (pentru opțiunea "Refuse" sau "Reset" din setări)
     */
    fun revokeConsent() {
        sharedPreferences.edit {
            putBoolean(KEY_CONSENT_GIVEN, false)
            // Nu ștergem KEY_CONSENT_ASKED pentru a nu deranja utilizatorul din nou
        }
        Log.d("InternetConsent", "❌ Internet consent REVOKED")
    }

    /**
     * ✅ Resetează complet (pentru debugging sau șters date aplicație)
     */
    fun resetConsent() {
        sharedPreferences.edit {
            remove(KEY_CONSENT_GIVEN)
            remove(KEY_CONSENT_TIMESTAMP)
            remove(KEY_CONSENT_ASKED)
        }
        Log.d("InternetConsent", "🔄 Internet consent RESET")
    }

    /**
     * ✅ Verifică dacă dispozitivul are conexiune la internet (HARDWARE)
     * Asta e diferit de consimțământ - verifică doar dacă există WiFi/Date mobile
     */
    fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * ✅ Verificare combinată: Consimțământ + Conexiune
     * Folosește asta înainte de orice operațiune Firebase/Maps
     */
    fun canUseInternet(): Boolean {
        val hasConsent = hasInternetConsent()
        val hasConnection = isInternetAvailable()

        Log.d("InternetConsent", "Can use internet? Consent=$hasConsent, Connection=$hasConnection")

        return hasConsent && hasConnection
    }

    /**
     * ✅ Obține timestamp-ul când a fost acordat consimțământul
     */
    fun getConsentTimestamp(): Long {
        return sharedPreferences.getLong(KEY_CONSENT_TIMESTAMP, 0L)
    }
}