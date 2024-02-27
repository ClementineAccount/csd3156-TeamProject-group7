package com.csd3156.team7

import android.graphics.Color
import android.util.Log
import com.csd3156.team7.Weather.Weather
import com.csd3156.team7.Weather.WeatherResponse
import com.csd3156.team7.Weather.WeatherServiceClient

class ShopItemRepository (private val shopItemDao: ShopItemDao, private val playerDao: PlayerDao)
{
    val allItems = shopItemDao.getAllItems()

    suspend fun insert(item: ShopItem) {
        shopItemDao.insert(item)
    }

    suspend fun delete(item: ShopItem) {
        shopItemDao.delete(item)
    }

    suspend fun deleteTable() {
        shopItemDao.deleteTable()
    }

    fun getItemByName(name: String): ShopItem {
        return shopItemDao.getItemByName(name)
    }

    fun updateQuantity(id: Int, quantity: Int) {
        shopItemDao.updateQuantity(id, quantity)
    }

    fun updateResearched(id: Int, researched: Boolean) {
        shopItemDao.updateResearched(id, researched)
    }

    suspend fun updateColor(id: Int, color: Int)
    {
        shopItemDao.updateColor(id, color)
    }

    fun getItemQuantity(id: Int): Int {
        return shopItemDao.getItemQuantity(id)
    }

    suspend fun getWeather(q : String): Weather {
        val response = WeatherServiceClient.create().getWeather(q)
        if (response.isSuccessful) {
            val weatherResponse: WeatherResponse = response.body()!!
            Log.d("WeatherService", "Success")
            Log.d("WeatherService", weatherResponse.weatherItems.latitude.toString())
            Log.d("WeatherService", weatherResponse.weatherItems.longitude.toString())

            return weatherResponse.weatherItems
        }
        else {
            Log.e("WeatherService", "Failed to get weather")
            return Weather("Error", "Error")
        }
    }
}