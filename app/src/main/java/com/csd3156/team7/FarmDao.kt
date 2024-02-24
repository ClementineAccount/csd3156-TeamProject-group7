package com.csd3156.team7

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM farmTable")
    fun getAll(): Flow<List<FarmItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(farm: FarmItem)

    @Query("DELETE FROM farmTable")
    suspend fun deleteAllFarm()
}