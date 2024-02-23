package com.csd3156.team7

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Dao

@Dao
interface PlayerDao {

    //@Query("SELECT playerItems FROM playerDatabase")
    //fun getAllItems(): LiveData<List<ShopItem>>

    @Query("SELECT startingCurrency FROM playerDatabase")
    fun getPlayerCurrency() : LiveData<Int>

    //@Insert
    //fun updateCurrency(player: Player)

    @Delete
    fun delete(digit: Player)

    @Query("DELETE FROM playerDatabase")
    fun deleteTable()
}