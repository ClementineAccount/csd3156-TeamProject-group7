package com.csd3156.team7

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ShopItemDao {

     @Query("SELECT * FROM itemTable ORDER BY itemID ASC")
     fun getAllItems(): LiveData<List<ShopItem>>

     @Query("SELECT * FROM itemTable WHERE itemID = :id")
     fun getItemById(id: Int): LiveData<ShopItem>

     @Query("SELECT * FROM itemTable WHERE itemName = :name")
     fun getItemByName(name: String): ShopItem


     // update quantity of item
     @Query("UPDATE itemTable SET quantity = :quantity WHERE itemName = :name")
     fun updateQuantity(name: String, quantity: Int)

     @Insert
     fun insert(item: ShopItem)

     @Delete
     fun delete(item: ShopItem)

     @Query("DELETE FROM itemTable")
     fun deleteTable()


}