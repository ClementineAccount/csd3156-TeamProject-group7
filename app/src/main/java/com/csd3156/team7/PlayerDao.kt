package com.csd3156.team7

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Dao

@Dao
interface PlayerDao {

//    @Query("SELECT playerItems FROM playerDatabase")
//    fun getAllItems(): LiveData<List<ShopItem>>

    @Query("SELECT startingCurrency FROM playerDatabase")
    fun getPlayerCurrency() : LiveData<Int>

    //    @Insert
    //    fun insertCurrency(currency: PlayerData)

    @Delete
    fun delete(digit: PlayerData)

    @Query("DELETE FROM playerDatabase")
    fun deleteTable()
}