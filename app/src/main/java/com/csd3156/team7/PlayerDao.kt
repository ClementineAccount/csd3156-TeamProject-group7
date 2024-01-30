package com.csd3156.team7

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Dao

@Dao
interface PlayerDao {

    @Query("SELECT * FROM playerDatabase")
    fun getAll(): LiveData<List<PlayerData>>

    @Query("SELECT PlayerItems FROM playerDatabase")
    fun getAllItems(): LiveData<List<ShopItem>>

    @Query("SELECT startingCurrency FROM playerDatabase")
    fun getPlayerCurrency() : LiveData<Int>

    @Query("SELECT * FROM playerDatabase WHERE playerName LIKE :name")
    fun findByName(name : String) : PlayerData

    @Insert
    fun insertCurrency(currency: PlayerData)

    @Delete
    fun delete(digit: PlayerData)

    @Query("DELETE FROM playerDatabase")
    fun deleteTable()
}