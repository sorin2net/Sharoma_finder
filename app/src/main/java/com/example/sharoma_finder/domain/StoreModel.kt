package com.example.sharoma_finder.domain

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Keep
@Entity(tableName = "stores")
data class StoreModel(
    @PrimaryKey
    var firebaseKey: String = "",

    var Id: Int = 0,

    // ✅ Păstrăm String, dar vom converti numeric → String în Repository
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

    var Tags: List<String> = emptyList()
) : Serializable {

    @Ignore
    var distanceToUser: Float = -1f

    fun getUniqueId(): String {
        return if (firebaseKey.isNotEmpty()) {
            firebaseKey
        } else {
            "${CategoryId}_${Id}"
        }
    }

    fun isValid(): Boolean {
        return Title.isNotBlank() &&
                Latitude != 0.0 &&
                Longitude != 0.0 &&
                CategoryId.isNotBlank() &&
                Address.isNotBlank()
    }

    fun hasTag(tagName: String): Boolean {
        return Tags.any { it.equals(tagName, ignoreCase = true) }
    }

    fun hasAnyTag(tagNames: List<String>): Boolean {
        return Tags.any { tag ->
            tagNames.any { it.equals(tag, ignoreCase = true) }
        }
    }
}