package com.csd3156.team7.Weather

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "weatherForecast")
data class Weather(val _description: String, val _lut: String) {
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
}

/*
* Be careful with the serialized name. It is the group header of the json response
* object.
*/
class WeatherResponse(@SerializedName("location")
                      val weatherItems: Weather = Weather("Error", "Error"),
                      @SerializedName("current")
                      val current: Current)

data class Current(
    @SerializedName("condition") // "text" is contained under "condition" in the json response
    val condition: Condition,

    // Include other fields from the `current` section as needed
    @SerializedName("last_updated")
    val lastUpdated: String
)
