package com.example.sharoma_finder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sharoma_finder.domain.BannerModel
import com.example.sharoma_finder.domain.CategoryModel
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.domain.SubCategoryModel

@Database(
    entities = [
        StoreModel::class,
        CategoryModel::class,
        BannerModel::class,
        SubCategoryModel::class  // ✅ ADĂUGAT
    ],
    version = 3,  // ✅ IMPORTANT: Crește versiunea de la 2 la 3
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    abstract fun categoryDao(): CategoryDao
    abstract fun bannerDao(): BannerDao
    abstract fun subCategoryDao(): SubCategoryDao  // ✅ ADĂUGAT

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sharoma_database"
                )
                    .fallbackToDestructiveMigration() // ✅ Resetează DB la upgrade
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}