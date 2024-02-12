package com.csd3156.team7
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "playerDatabase")
data class Player(var name: String, var startingCurrency: Int = 1000)
{
    @PrimaryKey(autoGenerate = true)
    var uid : Int = 0

    @ColumnInfo(name = "playerName")
    var playerName : String = name ?: "Player"

    @ColumnInfo(name = "currency")
    var currentCurrency : Int = startingCurrency

//    @ColumnInfo("playerItems")
//    var itemsOnHand = itemList
}