package com.example.sharoma_finder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sharoma_finder.domain.*

@Database(
    entities = [
        StoreModel::class,
        CategoryModel::class,
        BannerModel::class,
        SubCategoryModel::class,
        CacheMetadata::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    abstract fun categoryDao(): CategoryDao
    abstract fun bannerDao(): BannerDao
    abstract fun subCategoryDao(): SubCategoryDao
    abstract fun cacheMetadataDao(): CacheMetadataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS banners (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, image TEXT NOT NULL)")
                database.execSQL("CREATE TABLE IF NOT EXISTS categories (Id INTEGER PRIMARY KEY NOT NULL, ImagePath TEXT NOT NULL, Name TEXT NOT NULL)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS subcategories (Id INTEGER PRIMARY KEY NOT NULL, CategoryId TEXT NOT NULL, ImagePath TEXT NOT NULL, Name TEXT NOT NULL)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_subcategories_CategoryId ON subcategories(CategoryId)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS cache_metadata (`key` TEXT PRIMARY KEY NOT NULL, timestamp INTEGER NOT NULL, expiresAt INTEGER NOT NULL, itemCount INTEGER NOT NULL DEFAULT 0)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS stores_new (firebaseKey TEXT PRIMARY KEY NOT NULL, Id INTEGER NOT NULL, CategoryIds TEXT NOT NULL, SubCategoryIds TEXT NOT NULL, Title TEXT NOT NULL, Latitude REAL NOT NULL, Longitude REAL NOT NULL, Address TEXT NOT NULL, Call TEXT NOT NULL, Activity TEXT NOT NULL, ShortAddress TEXT NOT NULL, Hours TEXT NOT NULL, ImagePath TEXT NOT NULL, IsPopular INTEGER NOT NULL, Tags TEXT NOT NULL)")
                db.execSQL("INSERT INTO stores_new (firebaseKey, Id, CategoryIds, SubCategoryIds, Title, Latitude, Longitude, Address, Call, Activity, ShortAddress, Hours, ImagePath, IsPopular, Tags) SELECT firebaseKey, Id, '[\"' || CategoryId || '\"]', '[]', Title, Latitude, Longitude, Address, Call, Activity, ShortAddress, Hours, ImagePath, IsPopular, Tags FROM stores")
                db.execSQL("DROP TABLE stores")
                db.execSQL("ALTER TABLE stores_new RENAME TO stores")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS subcategories_new (Id INTEGER PRIMARY KEY NOT NULL, CategoryIds TEXT NOT NULL, ImagePath TEXT NOT NULL, Name TEXT NOT NULL)")
                db.execSQL("INSERT INTO subcategories_new (Id, CategoryIds, ImagePath, Name) SELECT Id, '[\"' || CategoryId || '\"]', ImagePath, Name FROM subcategories")
                db.execSQL("DROP TABLE subcategories")
                db.execSQL("ALTER TABLE subcategories_new RENAME TO subcategories")
            }
        }


        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stores_IsPopular ON stores(IsPopular)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stores_CategoryIds ON stores(CategoryIds)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stores_Tags ON stores(Tags)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sharoma_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7
                    )
                    //.fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getDatabaseVersion(context: Context): Int {
            return try {
                val db = getDatabase(context).openHelper.readableDatabase
                db.version
            } catch (e: Exception) { -1 }
        }
    }
}