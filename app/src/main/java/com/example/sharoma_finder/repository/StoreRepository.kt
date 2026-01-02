package com.example.sharoma_finder.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.sharoma_finder.data.CacheMetadataDao
import com.example.sharoma_finder.data.StoreDao
import com.example.sharoma_finder.domain.CacheMetadata
import com.example.sharoma_finder.domain.StoreModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class StoreRepository(
    private val storeDao: StoreDao,
    private val cacheMetadataDao: CacheMetadataDao
) {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private var lastRefreshTime = 0L
    private val MIN_REFRESH_INTERVAL = 300_000L // 5 minute

    val allStores: LiveData<List<StoreModel>> = storeDao.getAllStores()

    companion object {
        private const val CACHE_KEY_STORES = "stores"
        private const val CACHE_VALIDITY_HOURS = 6L
    }


    private suspend fun isCacheValid(): Boolean {
        return try {
            val metadata = cacheMetadataDao.getMetadata(CACHE_KEY_STORES)
            if (metadata == null) return false

            val now = System.currentTimeMillis()
            val isValid = now < metadata.expiresAt

            if (isValid) {
                val remainingMinutes = (metadata.expiresAt - now) / 60000
            } else {
            }
            isValid
        } catch (e: Exception) {
            false
        }
    }


    suspend fun refreshStores(forceRefresh: Boolean = false) {
        val now = System.currentTimeMillis()

        if (!forceRefresh && (now - lastRefreshTime) < MIN_REFRESH_INTERVAL) {
            val remaining = (MIN_REFRESH_INTERVAL - (now - lastRefreshTime)) / 1000
            return
        }

        withContext(Dispatchers.IO) {
            try {
                if (!forceRefresh && isCacheValid()) {
                    return@withContext
                }

                lastRefreshTime = now

                val snapshot = withTimeoutOrNull(15000L) {
                    firebaseDatabase.getReference("Stores").get().await()
                }

                if (snapshot == null || !snapshot.exists()) {
                    return@withContext
                }

                val freshStores = mutableListOf<StoreModel>()

                for (child in snapshot.children) {
                    val model = parseStoreFromSnapshot(child)
                    if (model != null && model.isValid()) {

                        model.firebaseKey = child.key ?: "${model.CategoryIds.firstOrNull()}_${model.Id}"
                        freshStores.add(model)
                    }
                }

                if (freshStores.isNotEmpty()) {
                    storeDao.insertAll(freshStores)

                    val expiresAt = now + (CACHE_VALIDITY_HOURS * 3600000) // 6 ore convertite in ms
                    cacheMetadataDao.saveMetadata(
                        CacheMetadata(
                            key = CACHE_KEY_STORES,
                            timestamp = now,
                            expiresAt = expiresAt,
                            itemCount = freshStores.size
                        )
                    )
                }

            } catch (e: Exception) {
                lastRefreshTime = 0L
            }
        }
    }


    private fun parseStoreFromSnapshot(snapshot: DataSnapshot): StoreModel? {
        return try {
            val map = snapshot.value as? Map<*, *> ?: return null

            val categoryIds = convertToList(map["CategoryIds"] ?: map["CategoryId"])
            val subCategoryIds = convertToList(map["SubCategoryIds"] ?: map["SubCategoryId"])
            val tags = convertToList(map["Tags"])

            val latitude = (map["Latitude"] as? Double) ?: 0.0
            val longitude = (map["Longitude"] as? Double) ?: 0.0

            if (latitude == 0.0 || longitude == 0.0) {
                return null
            }

            StoreModel(
                Id = (map["Id"] as? Long)?.toInt() ?: 0,
                CategoryIds = categoryIds,
                SubCategoryIds = subCategoryIds,
                Title = map["Title"] as? String ?: "",
                Address = map["Address"] as? String ?: "",
                ShortAddress = map["ShortAddress"] as? String ?: "",
                Activity = map["Activity"] as? String ?: "",
                Call = map["Call"] as? String ?: "",
                Hours = map["Hours"] as? String ?: "",
                Latitude = (map["Latitude"] as? Double) ?: 0.0,
                Longitude = (map["Longitude"] as? Double) ?: 0.0,
                ImagePath = map["ImagePath"] as? String ?: "",
                IsPopular = map["IsPopular"] as? Boolean ?: false,
                Tags = tags
            )
        } catch (e: Exception) {
            //fail fara logcat
            null
        }
    }


    private fun convertToList(data: Any?): List<String> {
        return when (data) {
            is List<*> -> data.mapNotNull { it?.toString() }
            is Long, is Int, is String -> listOf(data.toString())
            else -> emptyList()
        }
    }

    suspend fun hasCachedData(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                storeDao.getStoreCount() > 0
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            try {
                storeDao.deleteAll()
                cacheMetadataDao.deleteMetadata(CACHE_KEY_STORES)
            } catch (e: Exception) {
                //fail fara log
            }
        }
    }
}