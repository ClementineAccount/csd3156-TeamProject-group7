package com.csd3156.team7

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ShopItem::class], version = 2, exportSchema = false)
abstract class ShopItemDatabase : RoomDatabase()
{

    abstract fun shopItemDao(): ShopItemDao
    companion object {
        @Volatile
        private var INSTANCE: ShopItemDatabase? = null

        fun getDatabase(context: Context): ShopItemDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder (
                    context.applicationContext,
                    ShopItemDatabase::class.java,
                    "shopItemDatabase"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }


}