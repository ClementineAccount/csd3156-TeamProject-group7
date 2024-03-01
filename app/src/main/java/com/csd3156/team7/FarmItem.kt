package com.csd3156.team7
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Defines the FarmItem data class which is annotated as an Entity for Room database, indicating that this class will be a table in the database.
// Initializes various properties of the FarmItem with default values. These properties represent the columns in the farmTable.
@Entity(tableName = "farmTable")
data class FarmItem(var name: String = "Test", var lat: Double = 0.0, var long: Double = 0.0, var alt: Double = 0.0,
    var qx_set: Float = 0.0f, var qy_set: Float = 0.0f,
                    var qz_set: Float = 0.0f,
                    var qw_set: Float = 0.0f,
                    var shape: String = "Cube",
                    var rate : Float = 1.0f,
                    var growthTimeSet : Float = 3.0f)
{
    // Annotates uid as the primary key of the entity and enables auto-generating IDs to ensure uniqueness.
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    var uid : Long = 0

    // Additional columns in the farmTable, with default values derived from the primary constructor or specified defaults.
    @ColumnInfo(name = "farmName")
    var farmName : String = name ?: "My Test Farm" // Custom column name for the farm's name. Defaults to the name property if not specified.

    // Based off the items in ShopItem and ShopActivity
    @ColumnInfo(name = "farmShape")
    var farmShape: String = shape ?: "Cube" // Stores the shape of the farm item. Defaults to "Cube" if not specified.

    @ColumnInfo(name = "growthRate")
    var growthRate: Float = rate ?: 1.0f // Stores the growth rate of the farm item.

    // In Seconds
    @ColumnInfo(name = "growthTime")
    var growthTime : Float = growthTimeSet ?: 3.0f // Stores the growth time of the farm item, in seconds.

    // Columns for storing location and orientation details of the farm item.
    @ColumnInfo(name = "latitude")
    var latitude : Double = lat

    @ColumnInfo(name = "longtitude")
    var longtitude : Double = long

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