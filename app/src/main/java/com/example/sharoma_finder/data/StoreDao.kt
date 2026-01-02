package com.example.sharoma_finder.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sharoma_finder.domain.StoreModel

@Dao
interface StoreDao {
    @Query("SELECT * FROM stores")
    fun getAllStores(): LiveData<List<StoreModel>>

    @Query("SELECT * FROM stores")
    fun getAllStoresSync(): List<StoreModel>


    @Query("SELECT * FROM stores WHERE CategoryIds LIKE '%' || :catId || '%'")
    fun getStoresByCategory(catId: String): LiveData<List<StoreModel>>

    @Query("SELECT * FROM stores WHERE CategoryIds LIKE '%' || :catId || '%'")
    fun getStoresByCategorySync(catId: String): List<StoreModel>


    @Query("""
        SELECT * FROM stores 
        WHERE CategoryIds LIKE '%' || :catId || '%' 
        AND IsPopular = 1
    """)
    fun getPopularStoresByCategorySync(catId: String): List<StoreModel>


    @Query("""
        SELECT * FROM stores 
        WHERE CategoryIds LIKE '%' || :catId || '%' 
        AND Tags LIKE '%' || :tagName || '%'
    """)
    fun getStoresByTagSync(catId: String, tagName: String): List<StoreModel>


    @Query("""
        SELECT * FROM stores 
        WHERE CategoryIds LIKE '%' || :catId || '%' 
        AND IsPopular = 1 
        AND Tags LIKE '%' || :tagName || '%'
    """)
    fun getPopularStoresByTagSync(catId: String, tagName: String): List<StoreModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stores: List<StoreModel>)

    @Query("DELETE FROM stores")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM stores")
    fun getStoreCount(): Int
}