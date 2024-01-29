package com.csd3156.team7
import android.os.Parcel
import android.os.Parcelable
data class ShopItem(
    val name: String,
    val imageResourceId: Int,
    var quantity: Int,
    val description: String,
    val price: Int) : Parcelable
{




    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {

        dest.writeString(name)
        dest.writeInt(quantity)
        dest.writeInt(price)
    }

    companion object CREATOR : Parcelable.Creator<ShopItem> {
        override fun createFromParcel(parcel: Parcel): ShopItem {
            val name = parcel.readString()?: ""
            val imageResourceId = parcel.readInt()
            val quantity = parcel.readInt()
            val description = parcel.readString()?: ""
            val price = parcel.readInt()
            return ShopItem(name,imageResourceId, quantity, description, price )

        }

        override fun newArray(size: Int): Array<ShopItem?> {
            return arrayOfNulls(size)
        }
    }



}
