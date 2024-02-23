package com.csd3156.team7
import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itemTable")
data class ShopItem(
    val name: String,
    val imageResourceId: Int,
    var quantity: Int,
    val description: String,
    val price: Int,
    val researched: Boolean) : Parcelable
{
    @PrimaryKey(autoGenerate = true)
    var itemId = 0

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

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {

        dest.writeString(name)
        dest.writeInt(quantity)
        dest.writeInt(price)
        dest.writeBoolean(researched)
    }

    companion object CREATOR : Parcelable.Creator<ShopItem> {
        override fun createFromParcel(parcel: Parcel): ShopItem {
            val name = parcel.readString()?: ""
            val imageResourceId = parcel.readInt()
            val quantity = parcel.readInt()
            val description = parcel.readString()?: ""
            val price = parcel.readInt()
            val researched = parcel.readBoolean()
            return ShopItem(name,imageResourceId, quantity, description, price, researched)
        }

        override fun newArray(size: Int): Array<ShopItem?> {
            return arrayOfNulls(size)
        }
    }



}
