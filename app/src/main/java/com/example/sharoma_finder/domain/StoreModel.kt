package com.example.sharoma_finder.domain

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable


@Keep
@Entity(
    tableName = "stores",
    indices = [
        Index(value = ["IsPopular"]),
        Index(value = ["CategoryIds"]),
        Index(value = ["Tags"])
    ]
)
data class StoreModel(
    @PrimaryKey
    var firebaseKey: String = "",
    var Id: Int = 0,

    var CategoryIds: List<String> = emptyList(),

    var SubCategoryIds: List<String> = emptyList(),

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

    fun getUniqueId(): String = if (firebaseKey.isNotEmpty()) firebaseKey else "${CategoryIds.firstOrNull()}_${Id}"

    fun isValid(): Boolean {
        return Title.isNotBlank() &&
                Latitude != 0.0 &&
                Longitude != 0.0 &&
                CategoryIds.isNotEmpty() &&
                Address.isNotBlank()
    }


    @Ignore
    private var _cachedHasTag: MutableMap<String, Boolean>? = null

    fun hasTag(tagName: String): Boolean {
        if (_cachedHasTag == null) {
            _cachedHasTag = mutableMapOf()
        }

        return _cachedHasTag!!.getOrPut(tagName) {
            Tags.any { it.equals(tagName, ignoreCase = true) }
        }
    }

    fun getCleanPhoneNumber(): String {
        return Call.trim()
            .replace("-", "")
            .replace(" ", "")
    }

    fun hasValidPhoneNumber(): Boolean {
        val clean = getCleanPhoneNumber()
        return clean.matches(Regex("^[0-9+]{7,}$"))
    }

    fun hasAnyTag(tagNames: List<String>): Boolean {
        return Tags.any { tag ->
            tagNames.any { it.equals(tag, ignoreCase = true) }
        }
    }


    fun clearTagCache() {
        _cachedHasTag = null
    }
}