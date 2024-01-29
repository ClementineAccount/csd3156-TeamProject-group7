package com.csd3156.team7

import androidx.constraintlayout.helper.widget.Flow
import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Dao

@Dao
interface PlayerDao {

    @Query("SELECT * FROM playerDatabase")
    fun getAll(): LiveData<List<PlayerData>>

    @Query("SELECT * FROM playerDatabase WHERE playerName LIKE :name")
    fun findByName(name : String) : PlayerData

    @Insert
    fun insert(digit: PlayerData)

    @Delete
    fun delete(digit: PlayerData)

    @Query("DELETE FROM playerDatabase")
    fun deleteTable()
}