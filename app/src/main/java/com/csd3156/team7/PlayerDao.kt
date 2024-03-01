package com.csd3156.team7

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query

// Data Access Object (DAO) for Player entity
@Dao
interface PlayerDao {
    // Query to get the starting currency of the player as LiveData
    @Query("SELECT startingCurrency FROM playerDatabase")
    fun getPlayerCurrency() : LiveData<Int>

    // Query to update the starting currency of the player
    @Query("UPDATE playerDatabase SET startingCurrency = :newCurrency")
    fun updatePlayerCurrency(newCurrency: Int)

    // Delete operation for a Player entity
    @Delete
    fun delete(digit: Player)

    // Query to delete all records from the playerDatabase table
    @Query("DELETE FROM playerDatabase")
    fun deleteTable()
}