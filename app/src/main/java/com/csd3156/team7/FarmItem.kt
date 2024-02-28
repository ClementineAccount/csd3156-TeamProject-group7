package com.csd3156.team7
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// TODO: Also add type of farm item
@Entity(tableName = "farmTable")
data class FarmItem(var name: String = "Test", var lat: Double = 0.0, var long: Double = 0.0, var alt: Double = 0.0,
    var qx_set: Float = 0.0f, var qy_set: Float = 0.0f,
                    var qz_set: Float = 0.0f,
                    var qw_set: Float = 0.0f,
                    var shape: String = "Cube",
                    var rate : Float = 1.0f,
                    var growthTimeSet : Float = 3.0f)
{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    var uid : Long = 0

    @ColumnInfo(name = "farmName")
    var farmName : String = name ?: "My Test Farm"

    // Based off the items in ShopItem and ShopActivity
    @ColumnInfo(name = "farmShape")
    var farmShape: String = shape ?: "Cube"

    @ColumnInfo(name = "growthRate")
    var growthRate: Float = rate ?: 1.0f

    // In Seconds
    @ColumnInfo(name = "growthTime")
    var growthTime : Float = growthTimeSet ?: 3.0f

    // I only care to store longitude and latitude for this project
    @ColumnInfo(name = "latitude")
    var latitude : Double = lat

    @ColumnInfo(name = "longitude")
    var longitude : Double = long

    @ColumnInfo(name = "altitude")
    var altitude : Double = alt

    @ColumnInfo(name = "qx")
    var qx : Float = qx_set

    @ColumnInfo(name = "qy")
    var qy : Float = qy_set

    @ColumnInfo(name = "qz")
    var qz : Float = qz_set

    @ColumnInfo(name = "qw")
    var qw : Float = qw_set

}