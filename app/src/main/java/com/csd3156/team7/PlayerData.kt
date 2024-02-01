package com.csd3156.team7
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playerDatabase")
data class PlayerData(var name: String,var startingCurrency: Int = 1000)
{
    @PrimaryKey(autoGenerate = true)
    var uid : Int = 0

    var playerName : String = name ?: "Player"

    @ColumnInfo(name = "currency")
    var currentCurrency : Int = startingCurrency

//    @ColumnInfo("playerItems")
//    var itemsOnHand : Array<ShopItem> = items
}
