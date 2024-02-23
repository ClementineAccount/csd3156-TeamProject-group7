package com.csd3156.team7
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// TODO: Also add type of farm item
@Entity(tableName = "farmTable")
data class FarmItem(var name: String = "Test", var lat: Double = 0.0, var long: Double = 0.0)
{
    @PrimaryKey(autoGenerate = true)
    var uid : Int = 0

    @ColumnInfo(name = "farmName")
    var farmName : String = name ?: "My Test Farm"

    // I only care to store longitude and latitude for this project
    @ColumnInfo(name = "latitude")
    var latitude : Double = lat

    @ColumnInfo(name = "longitude")
    var longitude : Double = long
}