package com.example.sharoma_finder.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.example.sharoma_finder.data.SubCategoryDao
import com.example.sharoma_finder.domain.SubCategoryModel
import kotlinx.coroutines.Dispatchers

class ResultsRepository(private val subCategoryDao: SubCategoryDao) {


    fun loadSubCategory(id: String): LiveData<Resource<MutableList<SubCategoryModel>>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.Loading())

            try {
                val cachedData = subCategoryDao.getSubCategoriesByCategorySync(id)
                if (cachedData.isNotEmpty()) {
                    emit(Resource.Success(cachedData.toMutableList()))
                }

                emitSource(
                    subCategoryDao.getSubCategoriesByCategory(id).map { subCategories ->
                        if (subCategories.isNotEmpty()) {
                            Resource.Success(subCategories.toMutableList())
                        } else {
                            Resource.Success(mutableListOf())
                        }
                    }
                )
            } catch (e: Exception) {
                emit(Resource.Error("Failed to load subcategories: ${e.message}"))
            }
        }
    }
}