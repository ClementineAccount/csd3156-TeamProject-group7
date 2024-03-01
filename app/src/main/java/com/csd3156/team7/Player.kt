package com.csd3156.team7
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playerDatabase")
data class Player(var name: String, var startingCurrency: Int)
{
    @PrimaryKey(autoGenerate = true)
    var uid : Int = 0

    @ColumnInfo(name = "playerName")
    var playerName : String = name ?: "Player"

    @ColumnInfo(name = "currency")
    var currentCurrency : Int = startingCurrency

}