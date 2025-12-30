package com.example.sharoma_finder.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.sharoma_finder.data.BannerDao
import com.example.sharoma_finder.data.CategoryDao
import com.example.sharoma_finder.data.SubCategoryDao
import com.example.sharoma_finder.domain.BannerModel
import com.example.sharoma_finder.domain.CategoryModel
import com.example.sharoma_finder.domain.SubCategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class DashboardRepository(
    private val categoryDao: CategoryDao,
    private val bannerDao: BannerDao,
    private val subCategoryDao: SubCategoryDao
) {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    val allCategories: LiveData<List<CategoryModel>> = categoryDao.getAllCategories()
    val allBanners: LiveData<List<BannerModel>> = bannerDao.getAllBanners()

    suspend fun refreshCategories() {
        withContext(Dispatchers.IO) {
            try {
                Log.d("DashboardRepo", "🌍 Syncing categories...")

                val snapshot = withTimeoutOrNull(10000L) {
                    firebaseDatabase.getReference("Category").get().await()
                }

                if (snapshot == null) {
                    Log.w("DashboardRepo", "⏰ Category sync timeout")
                    return@withContext
                }

                val categories = mutableListOf<CategoryModel>()
                for (child in snapshot.children) {
                    child.getValue(CategoryModel::class.java)?.let { categories.add(it) }
                }

                if (categories.isNotEmpty()) {
                    categoryDao.insertAll(categories)
                    Log.d("DashboardRepo", "✅ Synced ${categories.size} categories")
                }

            } catch (e: Exception) {
                Log.e("DashboardRepo", "❌ Category sync failed: ${e.message}")
            }
        }
    }

    suspend fun refreshBanners() {
        withContext(Dispatchers.IO) {
            try {
                Log.d("DashboardRepo", "🌍 Syncing banners...")

                val snapshot = withTimeoutOrNull(10000L) {
                    firebaseDatabase.getReference("Banners").get().await()
                }

                if (snapshot == null) {
                    Log.w("DashboardRepo", "⏰ Banner sync timeout")
                    return@withContext
                }

                val banners = mutableListOf<BannerModel>()
                for (child in snapshot.children) {
                    child.getValue(BannerModel::class.java)?.let { banners.add(it) }
                }

                if (banners.isNotEmpty()) {
                    bannerDao.insertAll(banners)
                    Log.d("DashboardRepo", "✅ Synced ${banners.size} banners")
                }

            } catch (e: Exception) {
                Log.e("DashboardRepo", "❌ Banner sync failed: ${e.message}")
            }
        }
    }

    /**
     * ✅ FIX: Parsing manual pentru SubCategory (CategoryId poate fi Int/String)
     */
    suspend fun refreshSubCategories() {
        withContext(Dispatchers.IO) {
            try {
                Log.d("DashboardRepo", "🌍 Syncing subcategories...")

                val snapshot = withTimeoutOrNull(10000L) {
                    firebaseDatabase.getReference("SubCategory").get().await()
                }

                if (snapshot == null) {
                    Log.w("DashboardRepo", "⏰ SubCategory sync timeout")
                    return@withContext
                }

                val subCategories = mutableListOf<SubCategoryModel>()
                for (child in snapshot.children) {
                    val parsed = parseSubCategoryFromSnapshot(child)
                    if (parsed != null) {
                        subCategories.add(parsed)
                    }
                }

                if (subCategories.isNotEmpty()) {
                    subCategoryDao.insertAll(subCategories)
                    Log.d("DashboardRepo", "✅ Synced ${subCategories.size} subcategories")
                }

            } catch (e: Exception) {
                Log.e("DashboardRepo", "❌ SubCategory sync failed: ${e.message}")
            }
        }
    }

    /**
     * ✅ HELPER: Parsează SubCategory manual
     */
    /**
     * ✅ HELPER: Parsează SubCategory manual
     */
    private fun parseSubCategoryFromSnapshot(snapshot: DataSnapshot): SubCategoryModel? {
        return try {
            val map = snapshot.value as? Map<*, *> ?: return null

            // Citim noua listă "CategoryIds" sau vechiul "CategoryId" pentru compatibilitate
            // ✅ Acum apelăm funcția de mai jos
            val categoryIds = convertToList(map["CategoryIds"] ?: map["CategoryId"])

            SubCategoryModel(
                Id = (map["Id"] as? Long)?.toInt() ?: 0,
                CategoryIds = categoryIds,
                ImagePath = map["ImagePath"] as? String ?: "",
                Name = map["Name"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e("DashboardRepo", "Failed to parse SubCategory: ${e.message}")
            null
        }
    }

    // ✅ MUTATĂ AICI (în afara funcției de parsare, dar în interiorul clasei)
    private fun convertToList(data: Any?): List<String> {
        return when (data) {
            is List<*> -> data.mapNotNull { it?.toString() } // ✅ Scoate elementele null
            is Long, is Int, is String -> listOf(data.toString())
            else -> emptyList() // ✅ Acoperă și cazul 'null' global
        }
    }

    fun getSubCategoriesByCategory(categoryId: String): LiveData<List<SubCategoryModel>> {
        return subCategoryDao.getSubCategoriesByCategory(categoryId)
    }

    suspend fun hasCachedCategories(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                categoryDao.getCategoryCount() > 0
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun hasCachedBanners(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                bannerDao.getBannerCount() > 0
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun hasCachedSubCategories(categoryId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                subCategoryDao.getSubCategoryCount(categoryId) > 0
            } catch (e: Exception) {
                false
            }
        }
    }
}