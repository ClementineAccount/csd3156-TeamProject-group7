package com.csd3156.team7
import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playerDatabase")
data class PlayerData(var name: String,var currency: Int)
{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo
    var playerName : String = name ?: "Player"
    // there must be memory of this made

    @ColumnInfo(name = "currency")
    var currentCurrency : Int = currency

}
