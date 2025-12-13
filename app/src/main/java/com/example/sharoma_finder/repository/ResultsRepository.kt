package com.example.sharoma_finder.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sharoma_finder.data.SubCategoryDao
import com.example.sharoma_finder.domain.SubCategoryModel

/**
 * ✅ VERSIUNE OFFLINE-FIRST COMPLETĂ
 *
 * Repository-ul ăsta încarcă subcategoriile din CACHE (Room)
 * Sincronizarea cu Firebase se face prin DashboardRepository
 */
class ResultsRepository(private val subCategoryDao: SubCategoryDao) {

    /**
     * ✅ Încarcă subcategoriile (Burger, Pizza, Sushi) din CACHE LOCAL
     * Acestea sunt necesare doar pentru filtrarea UI-ului
     */
    fun loadSubCategory(id: String): LiveData<Resource<MutableList<SubCategoryModel>>> {
        val listData = MutableLiveData<Resource<MutableList<SubCategoryModel>>>()
        listData.value = Resource.Loading()

        try {
            // ✅ SCHIMBARE MAJORĂ: Încărcăm din ROOM, nu din Firebase
            val liveData = subCategoryDao.getSubCategoriesByCategory(id)

            // Transformăm LiveData<List<SubCategoryModel>> în Resource
            liveData.observeForever { subCategories ->
                if (subCategories != null) {
                    val mutableList = subCategories.toMutableList()
                    Log.d("ResultsRepository", "✅ Loaded ${mutableList.size} subcategories from cache for category $id")
                    listData.value = Resource.Success(mutableList)
                } else {
                    Log.w("ResultsRepository", "⚠️ No subcategories found in cache for category $id")
                    listData.value = Resource.Success(mutableListOf())
                }
            }
        } catch (e: Exception) {
            Log.e("ResultsRepository", "❌ Error loading subcategories from cache: ${e.message}")
            listData.value = Resource.Success(mutableListOf())
        }

        return listData
    }
}