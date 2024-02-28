package com.csd3156.team7

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PlayerCurrencyObject {
    var currency = 1000
}

class PlayerShopViewModel(application: Application) : AndroidViewModel(application) {
    private val dao: PlayerDao
    var repository: PlayerRepository
    var currentPlayerCurrency : LiveData<Int>

    private val itemDao: ShopItemDao
    val shopRepository: ShopItemRepository
    val allItems: LiveData<List<ShopItem>>

    var playerCurrencyObject : PlayerCurrencyObject = PlayerCurrencyObject

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

    fun setPlayerCurrency(newCurrency: Int)
    {
        viewModelScope.launch(Dispatchers.IO) { repository.setPlayerCurrency(newCurrency)}
        playerCurrencyObject.currency = newCurrency
    }

    fun updatePlayerCurrency(newCurrency: Int) {viewModelScope.launch(Dispatchers.IO) {dao.updatePlayerCurrency(newCurrency)}}

    fun insertPlayer(value: Player) { InsertAsyncTask(dao).execute(value) }

    fun delete(value: Player) { dao.delete(value) }

    fun deleteTable() { DeleteTableAsyncTask(dao).execute() }
    fun insertItem(shopItem: ShopItem) {
        viewModelScope.launch(Dispatchers.IO) {
            shopRepository.insert(shopItem)
        }
    }

    fun findItem(name : String) : ShopItem {
        return shopRepository.getItemByName(name)
    }

    fun deleteItem(shopItem: ShopItem) {
        viewModelScope.launch(Dispatchers.IO) {
            shopRepository.delete(shopItem)
        }
    }

    fun deleteItemTable() {
        viewModelScope.launch(Dispatchers.IO) {
            shopRepository.deleteTable()
        }
    }

    fun getItemQuantity(id : Int) : Int {
        return shopRepository.getItemQuantity(id)
    }

    fun updateItemQuantity(id : Int, quantity : Int) {
        viewModelScope.launch(Dispatchers.IO) {
            shopRepository.updateQuantity(id, quantity)
        }
    }

    fun updateItemResearchState(id : Int, research : Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            shopRepository.updateResearched(id,research)
        }
    }

    fun updateItemColor(id : Int, color : Int) {
        viewModelScope.launch(Dispatchers.IO) {
            shopRepository.updateColor(id,color)
        }
    }




    private class DeleteTableAsyncTask(private val dao: PlayerDao) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? { dao.deleteTable(); return null }
    }

    private class InsertAsyncTask(dao: PlayerDao) : AsyncTask<Player, Void, Void>() {
        @Deprecated("Deprecated in Java", ReplaceWith("null"))
        public override fun doInBackground(vararg params: Player): Void? {
//            dao.insert(params[0])
            return null
        }
    }
}

