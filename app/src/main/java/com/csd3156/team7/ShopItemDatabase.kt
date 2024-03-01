package com.csd3156.team7

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Define the Room database with ShopItem entity
@Database(entities = [ShopItem::class], version = 3, exportSchema = true)
abstract class  ShopItemDatabase : RoomDatabase()
{

    // Define the abstract function to get the ShopItemDao
    abstract fun shopItemDao(): ShopItemDao
    companion object {
        @Volatile
        private var INSTANCE: ShopItemDatabase? = null

        // Define a migration from version 1 to version 2
        private val migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE itemTable ADD COLUMN itemResearched INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Define a migration from version 2 to version 3
        private val migration2 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE itemTable ADD COLUMN researchCreditRequirement INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Get or create the database instance
        fun getDatabase(context: Context): ShopItemDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShopItemDatabase::class.java,
                    "shop_item_database")
                    .addMigrations(migration)
                    .addMigrations(migration2)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }



}