package com.csd3156.team7
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playerDatabase")
data class PlayerData(var name: String,var startingCurrency: Int)
{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo
    var playerName : String = name ?: "Player"

    @ColumnInfo(name = "currency")
    var currentCurrency : Int = startingCurrency

    @ColumnInfo("PlayerItems")
    var itemsOnHand : Array<ShopItem>
    = arrayOf(ShopItem("S",0,0,"",0))
}
