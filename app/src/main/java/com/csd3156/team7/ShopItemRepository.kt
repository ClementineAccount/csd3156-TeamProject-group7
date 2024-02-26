package com.csd3156.team7

import android.graphics.Color

class ShopItemRepository (private val shopItemDao: ShopItemDao, private val playerDao: PlayerDao)
{
    val allItems = shopItemDao.getAllItems()
    val playerCurrency = playerDao.getPlayerCurrency()

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
}