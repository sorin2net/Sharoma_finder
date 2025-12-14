package com.example.sharoma_finder.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.sharoma_finder.data.StoreDao
import com.example.sharoma_finder.domain.StoreModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class StoreRepository(private val storeDao: StoreDao) {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    val allStores: LiveData<List<StoreModel>> = storeDao.getAllStores()

    /**
     * ✅ VERSIUNE FINALĂ: Parsing manual pentru a gestiona CategoryId numeric
     */
    suspend fun refreshStores() {
        withContext(Dispatchers.IO) {
            try {
                Log.d("StoreRepository", "🌍 Starting Firebase sync...")

                val snapshot = withTimeoutOrNull(15000L) {
                    firebaseDatabase.getReference("Stores").get().await()
                }

                if (snapshot == null) {
                    Log.w("StoreRepository", "⏰ Firebase timeout - using cache")
                    return@withContext
                }

                Log.d("StoreRepository", "📦 Snapshot exists: ${snapshot.exists()}, children: ${snapshot.childrenCount}")

                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    Log.w("StoreRepository", "⚠️ Firebase returned empty - keeping cache")
                    return@withContext
                }

                val freshStores = mutableListOf<StoreModel>()
                var invalidCount = 0
                var parseErrorCount = 0

                for (child in snapshot.children) {
                    try {
                        // ✅ PARSING MANUAL pentru a gestiona CategoryId numeric/String
                        val model = parseStoreFromSnapshot(child)

                        if (model == null) {
                            parseErrorCount++
                            Log.w("StoreRepository", "⚠️ Failed to parse: ${child.key}")
                            continue
                        }

                        if (model.isValid()) {
                            model.firebaseKey = child.key ?: "${model.CategoryId}_${model.Id}"
                            freshStores.add(model)
                        } else {
                            invalidCount++
                            Log.w("StoreRepository", "⚠️ Invalid: ${model.Title}")
                        }
                    } catch (e: Exception) {
                        parseErrorCount++
                        Log.e("StoreRepository", "❌ Parse error for ${child.key}: ${e.message}")
                    }
                }

                Log.d("StoreRepository", "📊 Results: ✅ ${freshStores.size} valid, ⚠️ $invalidCount invalid, ❌ $parseErrorCount errors")

                if (freshStores.isEmpty()) {
                    Log.e("StoreRepository", "❌ ZERO valid stores - keeping cache")
                    return@withContext
                }

                // ✅ Salvăm datele valide
                storeDao.insertAll(freshStores)
                Log.d("StoreRepository", "💾 Saved ${freshStores.size} stores to cache")

            } catch (e: Exception) {
                Log.e("StoreRepository", "❌ Error: ${e.javaClass.simpleName} - ${e.message}")
            }
        }
    }

    /**
     * ✅ FUNCȚIE HELPER: Parsează manual un store din Firebase
     * Gestionează CategoryId atât ca Int cât și ca String
     */
    private fun parseStoreFromSnapshot(snapshot: DataSnapshot): StoreModel? {
        try {
            val map = snapshot.value as? Map<*, *> ?: return null

            // ✅ Convertește CategoryId (poate fi Int sau String)
            val categoryId = when (val catId = map["CategoryId"]) {
                is Long -> catId.toString()
                is Int -> catId.toString()
                is String -> catId
                else -> ""
            }

            // ✅ Parsează Tags (poate fi List sau null)
            val tags = when (val tagData = map["Tags"]) {
                is List<*> -> tagData.mapNotNull { it as? String }
                else -> emptyList()
            }

            return StoreModel(
                Id = (map["Id"] as? Long)?.toInt() ?: 0,
                CategoryId = categoryId,
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
            Log.e("StoreRepository", "Parse exception: ${e.message}")
            return null
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
                Log.d("StoreRepository", "🗑️ Cache cleared")
            } catch (e: Exception) {
                Log.e("StoreRepository", "Error clearing cache: ${e.message}")
            }
        }
    }
}