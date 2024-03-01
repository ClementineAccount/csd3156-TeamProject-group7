package com.csd3156.team7

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Defines an interface for accessing the farmTable in the database using DAO (Data Access Object) pattern.
@Dao
interface FarmDao {
    // A query method to get all farm items from farmTable. It returns a Flow list of FarmItem objects.
    // Flow is a type that can emit multiple values sequentially, as opposed to LiveData which is lifecycle-aware.
    @Query("SELECT * FROM farmTable")
    fun getAll(): Flow<List<FarmItem>>

    // Insert method to add a new FarmItem into farmTable. If the farm item already exists (based on primary key or any unique constraint),
    // it ignores the new item to prevent conflict. This method is a suspend function, making it safe to call from a coroutine without blocking the main thread.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(farm: FarmItem) : Long

    // A query method to delete all farm items from farmTable. It's a suspend function to ensure it can be called within a coroutine,
    // making the operation non-blocking.
    @Query("DELETE FROM farmTable")
    suspend fun deleteAllFarm()

    // A query method to fetch the first farm item from farmTable. It returns LiveData of a single FarmItem.
    // LiveData is lifecycle-aware, meaning it only updates app component observers that are in an active lifecycle state.
    @Query("SELECT * FROM farmTable LIMIT 1")
    fun getFirstFarm(): LiveData<FarmItem>
}