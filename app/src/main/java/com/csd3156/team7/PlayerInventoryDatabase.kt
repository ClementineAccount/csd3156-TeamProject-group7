package com.csd3156.team7

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// The player's save data, such as their inventory and the collectables
// they have obtained, may be saved in a database.

//@Database(entities = [Player::class], version = 2, exportSchema = false)
// Currently this is named 'PlayerInventoryDatabase' but it also store the Farm Item.
// The name is a bit wrong but it is kept for backwards compatbility.
// Database class for storing player's save data, including inventory and collectibles
// This database includes entities: Player and FarmItem
@Database(entities = [Player::class, FarmItem::class], version = 9, exportSchema = false)
abstract class PlayerInventoryDatabase : RoomDatabase()
{
    // Abstract function to get the DAO (Data Access Object) for FarmItem
    abstract fun farmDao(): FarmDao

    // Abstract function to get the DAO for Player
    abstract fun playerDao(): PlayerDao

    // Companion object to implement the Singleton pattern for database instance
    companion object {
        // Volatile variable to ensure the visibility of changes across threads
        @Volatile
        private var INSTANCE: PlayerInventoryDatabase? = null

        // Function to get the database instance
        fun getDatabase(context: Context): PlayerInventoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder (
                    context.applicationContext,
                    PlayerInventoryDatabase::class.java,
                    "playerDatabase"
                ).fallbackToDestructiveMigration().build()

                // Set the instance and return
                INSTANCE = instance
                instance
            }
        }
    }
}