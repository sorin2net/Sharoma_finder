package com.example.sharoma_finder.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sharoma_finder.domain.SubCategoryModel

@Dao
interface SubCategoryDao {
    @Query("SELECT * FROM subcategories WHERE CategoryId = :categoryId ORDER BY Id ASC")
    fun getSubCategoriesByCategory(categoryId: String): LiveData<List<SubCategoryModel>>

    @Query("SELECT * FROM subcategories WHERE CategoryId = :categoryId ORDER BY Id ASC")
    fun getSubCategoriesByCategorySync(categoryId: String): List<SubCategoryModel>

    @Query("SELECT * FROM subcategories ORDER BY Id ASC")
    fun getAllSubCategories(): LiveData<List<SubCategoryModel>>

    @Query("SELECT * FROM subcategories ORDER BY Id ASC")
    fun getAllSubCategoriesSync(): List<SubCategoryModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subCategories: List<SubCategoryModel>)

    @Query("DELETE FROM subcategories")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM subcategories WHERE CategoryId = :categoryId")
    fun getSubCategoryCount(categoryId: String): Int
}