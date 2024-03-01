package com.csd3156.team7

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.csd3156.team7.Weather.Weather
import com.csd3156.team7.Weather.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Object representing the player's currency
object PlayerCurrencyObject {
    var currency = 1000
}

// ViewModel for the player's shop
class PlayerShopViewModel(application: Application) : AndroidViewModel(application) {
    private val dao: PlayerDao
    var repository: PlayerRepository
    var currentPlayerCurrency : LiveData<Int>

    private val itemDao: ShopItemDao
    val shopRepository: ShopItemRepository
    val allItems: LiveData<List<ShopItem>>

    var playerCurrencyObject : PlayerCurrencyObject = PlayerCurrencyObject

    // Initialize the ViewModel
    init {
        val database = PlayerInventoryDatabase.getDatabase(application)
        dao = database.playerDao()
        repository = PlayerRepository(application)

        currentPlayerCurrency = repository.getPlayerCurrency().asLiveData()

        val shopDatabase = ShopItemDatabase.getDatabase(application)
        itemDao = shopDatabase.shopItemDao()
        shopRepository = ShopItemRepository(itemDao, dao)
        allItems = shopRepository.allItems
    }

    // Set the player's currency
    fun setPlayerCurrency(newCurrency: Int)
    {
        viewModelScope.launch(Dispatchers.IO) { repository.setPlayerCurrency(newCurrency)}
        playerCurrencyObject.currency = newCurrency
    }

    // Update the player's currency
    fun updatePlayerCurrency(newCurrency: Int) {viewModelScope.launch(Dispatchers.IO) {dao.updatePlayerCurrency(newCurrency)}}

    // Insert a player into the database
    fun insertPlayer(value: Player) { InsertAsyncTask(dao).execute(value) }

    // Delete a player from the database
    fun delete(value: Player) { dao.delete(value) }

    // Delete the entire table
    fun deleteTable() { DeleteTableAsyncTask(dao).execute() }

    // Insert a shop item into the database
    fun insertItem(shopItem: ShopItem) {
        viewModelScope.launch(Dispatchers.IO) {
            shopRepository.insert(shopItem)
        }
    }

    // Find a shop item by name
    fun findItem(name : String) : ShopItem {
        return shopRepository.getItemByName(name)
    }

    // Delete a shop item from the database
    fun deleteItem(shopItem: ShopItem) {
        viewModelScope.launch(Dispatchers.IO) {
            shopRepository.delete(shopItem)
        }
    }

    // Delete the entire shop item table
    fun deleteItemTable() {
        viewModelScope.launch(Dispatchers.IO) {
            shopRepository.deleteTable()
        }
    }

    // Get the quantity of a shop item by ID
    fun getItemQuantity(id : Int) : Int {
        return shopRepository.getItemQuantity(id)
    }

    // Update the quantity of a shop item
    fun updateItemQuantity(id : Int, quantity : Int) {
        viewModelScope.launch(Dispatchers.IO) {
            shopRepository.updateQuantity(id, quantity)
        }
    }

    // Update the research state of a shop item
    fun updateItemResearchState(id : Int, research : Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            shopRepository.updateResearched(id,research)
        }
    }

    // Update the color of a shop item
    fun updateItemColor(id : Int, color : Int) {
        viewModelScope.launch(Dispatchers.IO) {
            shopRepository.updateColor(id,color)
        }
    }

    // Update the price of a shop item
    fun updateItemPrice(id : Int, price : Int) {
        viewModelScope.launch(Dispatchers.IO) {
            shopRepository.updatePrice(id,price)
        }
    }

    // Get the RGB components of a color
    fun getColorComponents(colorInt: Int): Triple<Int, Int, Int> {
        val red = (colorInt shr 16) and 0xff
        val green = (colorInt shr 8) and 0xff
        val blue = colorInt and 0xff
        return Triple(red, green, blue)
    }

    // Get the weather information
    suspend fun getWeather(q : String): WeatherResponse {
        return shopRepository.getWeather(q)
    }

    // AsyncTask to delete the entire table
    private class DeleteTableAsyncTask(private val dao: PlayerDao) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? { dao.deleteTable(); return null }
    }

    // AsyncTask to insert a player
    private class InsertAsyncTask(dao: PlayerDao) : AsyncTask<Player, Void, Void>() {
        @Deprecated("Deprecated in Java", ReplaceWith("null"))
        public override fun doInBackground(vararg params: Player): Void? {
//            dao.insert(params[0])
            return null
        }
    }
}

