package com.csd3156.team7

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

    fun updateQuantity(name: String, quantity: Int) {
        shopItemDao.updateQuantity(name, quantity)
    }
}