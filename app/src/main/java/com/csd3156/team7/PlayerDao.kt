package com.csd3156.team7

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query

@Dao
interface PlayerDao {
    @Query("SELECT startingCurrency FROM playerDatabase")
    fun getPlayerCurrency() : LiveData<Int>

    @Query("UPDATE playerDatabase SET startingCurrency = :newCurrency")
    fun updatePlayerCurrency(newCurrency: Int)

    @Delete
    fun delete(digit: Player)

    @Query("DELETE FROM playerDatabase")
    fun deleteTable()
}