package com.csd3156.team7

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ShopItem::class], version = 3, exportSchema = true)
abstract class ShopItemDatabase : RoomDatabase()
{


    abstract fun shopItemDao(): ShopItemDao
    companion object {
        @Volatile
        private var INSTANCE: ShopItemDatabase? = null

        private val migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE itemTable ADD COLUMN itemResearched INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val migration2 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE itemTable ADD COLUMN researchCreditRequirement INTEGER NOT NULL DEFAULT 0")
            }
        }

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