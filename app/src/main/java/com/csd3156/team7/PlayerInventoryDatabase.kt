package com.csd3156.team7

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// The player's save data, such as their inventory and the collectables
// they have obtained, may be saved in a database.

// Currently this is named 'PlayerInventoryDatabase' but it also store the Farm Item.
// The name is a bit wrong but it is kept for backwards compatbility.
@Database(entities = [Player::class, FarmItem::class], version = 2, exportSchema = false)
abstract class PlayerInventoryDatabase : RoomDatabase()
{
    abstract fun farmDao(): FarmDao

    abstract fun playerDao(): PlayerDao
    companion object {
        @Volatile
        private var INSTANCE: PlayerInventoryDatabase? = null

        fun getDatabase(context: Context): PlayerInventoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder (
                    context.applicationContext,
                    PlayerInventoryDatabase::class.java,
                    "playerDatabase"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}