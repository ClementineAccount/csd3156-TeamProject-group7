package com.csd3156.team7

import android.graphics.Color
import android.util.Log
import com.csd3156.team7.Weather.Weather
import com.csd3156.team7.Weather.WeatherResponse
import com.csd3156.team7.Weather.WeatherServiceClient

class ShopItemRepository (private val shopItemDao: ShopItemDao, private val playerDao: PlayerDao)
{
    // LiveData representing all items in the shop
    val allItems = shopItemDao.getAllItems()

    // Insert a new shop item into the database
    fun insert(item: ShopItem) { shopItemDao.insert(item) }

    // Delete a shop item from the database
    fun delete(item: ShopItem) { shopItemDao.delete(item) }

    // Delete the entire shop item table
    fun deleteTable() { shopItemDao.deleteTable() }

    // Get a shop item by name
    fun getItemByName(name: String): ShopItem {
        return shopItemDao.getItemByName(name)
    }

    // Update the quantity of a shop item
    suspend fun updateQuantity(id: Int, quantity: Int) {
        shopItemDao.updateQuantity(id, quantity)
    }

    // Update the researched status of a shop item
    fun updateResearched(id: Int, researched: Boolean) {
        shopItemDao.updateResearched(id, researched)
    }

    // Update the color of a shop item
    fun updateColor(id: Int, color: Int) {
        shopItemDao.updateColor(id, color)
    }

    // Update the price of a shop item
    fun updatePrice(id: Int, price: Int) {
        shopItemDao.updatePrice(id, price)
    }

    // Get the quantity of a shop item
    fun getItemQuantity(id: Int): Int {
        return shopItemDao.getItemQuantity(id)
    }

    // Retrieve weather information using the WeatherService API
    suspend fun getWeather(q : String): WeatherResponse {
        val response = WeatherServiceClient.create().getWeather(q)
        if (response.isSuccessful) {
            val weatherResponse: WeatherResponse = response.body()!!
            Log.d("WeatherService", "Retrieval Success.")
//            Log.d("WeatherService", weatherResponse.weatherItems.latitude.toString())
//            Log.d("WeatherService", weatherResponse.weatherItems.longitude.toString())
//            Log.d("WeatherService", weatherResponse.current.lastUpdated)

            return weatherResponse
        }
        else {
            Log.e("WeatherService", "Failed to get weather")
            throw Exception("Failed to get weather")
        }
    }
}