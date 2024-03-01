package com.csd3156.team7
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Entity class representing a Player
@Entity(tableName = "playerDatabase")
data class Player(var name: String, var startingCurrency: Int)
{
    // Auto-generated primary key
    @PrimaryKey(autoGenerate = true)
    var uid : Int = 0

    // Custom column name for player name
    @ColumnInfo(name = "playerName")
    var playerName : String = name ?: "Player"

    // Custom column name for current currency
    @ColumnInfo(name = "currency")
    var currentCurrency : Int = startingCurrency

}