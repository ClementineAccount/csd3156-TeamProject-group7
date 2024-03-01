package com.csd3156.team7

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Define the Data Access Object (DAO) for ShopItem entity
@Dao
interface ShopItemDao {

     // Query to get all items ordered by itemPrice in ascending order
     @Query("SELECT * FROM itemTable ORDER BY itemPrice ASC")
     fun getAllItems(): LiveData<List<ShopItem>>

     // Query to get an item by its ID
     @Query("SELECT * FROM itemTable WHERE itemID = :id")
     fun getItemById(id: Int): LiveData<ShopItem>

     // Query to get an item by its name
     @Query("SELECT * FROM itemTable WHERE itemName = :name")
     fun getItemByName(name: String): ShopItem

     // Query to update the quantity of an item by its ID (suspend function for coroutines)
     @Query("UPDATE itemTable SET quantity = :quantity WHERE itemID = :id")
     suspend fun updateQuantity(id: Int, quantity: Int)

     // Query to update the researched status of an item by its ID
     @Query("UPDATE itemTable SET researched = :isResearched WHERE itemID = :id")
     fun updateResearched(id: Int, isResearched: Boolean)

     // Query to update the color of an item by its ID
     @Query("UPDATE itemTable SET color = :color WHERE itemID = :id")
     fun updateColor(id: Int, color: Int)

     // Query to update the price of an item by its ID
     @Query("UPDATE itemTable SET price = :price WHERE itemID = :id")
     fun updatePrice(id: Int, price: Int)

     // Insert operation with conflict strategy set to REPLACE
     @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insert(item: ShopItem)

     // Delete operation
     @Delete
     fun delete(item: ShopItem)

     // Query to delete all items from the table
     @Query("DELETE FROM itemTable")
     fun deleteTable()

     // Query to get the quantity of an item by its ID
     @Query("SELECT quantity FROM itemTable WHERE itemID = :id")
     fun getItemQuantity(id: Int): Int



}