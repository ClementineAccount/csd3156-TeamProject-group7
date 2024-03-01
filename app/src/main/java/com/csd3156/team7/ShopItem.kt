package com.csd3156.team7
import android.graphics.Color
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Define the ShopItem entity for Room database
@Entity(tableName = "itemTable")
data class ShopItem(
    val name: String,
    val imageResourceId: Int,
    var quantity: Int,
    val description: String,
    var price: Int,
    var researched: Boolean,
    val researchCreditRequirement: Int = 0,
    var color : Int
    ) : Parcelable
{
    // Auto-generated primary key for the entity
    @PrimaryKey(autoGenerate = true)
    var itemId = 0

    // Additional column information for Room database
    @ColumnInfo(name = "itemName")
    var itemName = name

    @ColumnInfo(name = "itemImage")
    var itemQuantity = quantity

    @ColumnInfo(name = "itemDescription")
    var itemDescription = description

    @ColumnInfo(name = "itemPrice")
    var buyPrice = price

    @ColumnInfo(name = "itemResearched")
    var itemResearched = researched

    @ColumnInfo(name = "researchCreditReq")
    var creditsToResearch = researchCreditRequirement

    @ColumnInfo(name = "itemColor")
    var itemColor = color


    // Implementation of Parcelable interface
    override fun describeContents(): Int {
        return 0
    }

    // Write object state to a parcel (serialization)
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(dest: Parcel, flags: Int) {

        dest.writeString(name)
        dest.writeInt(quantity)
        dest.writeInt(price)
        dest.writeBoolean(researched)
        dest.writeInt(creditsToResearch)
    }

    // Parcelable companion object for deserialization
    companion object CREATOR : Parcelable.Creator<ShopItem> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): ShopItem {
            val name = parcel.readString()?: ""
            val imageResourceId = parcel.readInt()
            val quantity = parcel.readInt()
            val description = parcel.readString()?: ""
            val price = parcel.readInt()
            val researched = parcel.readBoolean()
            val creditsToResearch = parcel.readInt()
            val color = parcel.readInt()
            return ShopItem(name,imageResourceId, quantity, description, price, researched, creditsToResearch, color)
        }

        // Parcelable array creation
        override fun newArray(size: Int): Array<ShopItem?> {
            return arrayOfNulls(size)
        }
    }



}
