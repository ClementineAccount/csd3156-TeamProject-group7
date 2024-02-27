package com.csd3156.team7.Weather

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "weatherForecast")
data class Weather(val _description: String, val _lut: String)
{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    @SerializedName("id")
    var id: Int = 0

    // latitude
    @ColumnInfo("latitude")
    @SerializedName("lat")
    var latitude: Double = 0.0

    // longitude
    @ColumnInfo("longitude")
    @SerializedName("lon")
    var longitude: Double = 0.0

    @ColumnInfo("text")
    @SerializedName("text")
    var description: String = _description

    // time
    @ColumnInfo("lastUpdateTime")
    @SerializedName("last_updated")
    var lastUpdateTime: String = _lut
}

/*
* Be careful with the serialized name. It is the group header of the json response
* object.
*/
class WeatherResponse(@SerializedName("location") val weatherItems: Weather = Weather("Error", "Error"))