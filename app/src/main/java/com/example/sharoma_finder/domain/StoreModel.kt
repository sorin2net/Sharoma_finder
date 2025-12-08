package com.example.sharoma_finder.domain

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class StoreModel(
    var Id: Int = 0,
    var CategoryId: String = "",
    var Title: String = "",
    var Latitude: Double = 0.0,
    var Longitude: Double = 0.0,
    var Address: String = "",
    var Call: String = "",
    var Activity: String = "",
    var ShortAddress: String = "",
    var Hours: String = "",
    var ImagePath: String = "",
    var IsPopular: Boolean = false,
    var firebaseKey: String = ""
) : Serializable {

    var distanceToUser: Float = -1f

    fun getUniqueId(): String {
        return if (firebaseKey.isNotEmpty()) {
            firebaseKey
        } else {
            "${CategoryId}_${Id}"
        }
    }

    // ✅ ADĂUGAT: Funcție de validare
    fun isValid(): Boolean {
        return Title.isNotBlank() &&
                Latitude != 0.0 &&
                Longitude != 0.0 &&
                CategoryId.isNotBlank() &&
                Address.isNotBlank()
    }
}