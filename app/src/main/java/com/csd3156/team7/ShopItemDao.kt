package com.csd3156.team7

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ShopItemDao {

     @Query("SELECT * FROM itemTable ORDER BY itemPrice ASC")
     fun getAllItems(): LiveData<List<ShopItem>>

     @Query("SELECT * FROM itemTable WHERE itemID = :id")
     fun getItemById(id: Int): LiveData<ShopItem>

     @Query("SELECT * FROM itemTable WHERE itemName = :name")
     fun getItemByName(name: String): ShopItem

     @Query("UPDATE itemTable SET quantity = :quantity WHERE itemID = :id")
     fun updateQuantity(id: Int, quantity: Int)

     @Query("UPDATE itemTable SET researched = :isResearched WHERE itemID = :id")
     fun updateResearched(id: Int, isResearched: Boolean)

     @Query("UPDATE itemTable SET color = :color WHERE itemID = :id")
     fun updateColor(id: Int, color: Int)

     @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insert(item: ShopItem)

     @Delete
     fun delete(item: ShopItem)

     @Query("DELETE FROM itemTable")
     fun deleteTable()

     @Query("SELECT quantity FROM itemTable WHERE itemID = :id")
     fun getItemQuantity(id: Int): Int



}