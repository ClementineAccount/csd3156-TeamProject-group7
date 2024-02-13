package com.csd3156.team7

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayerShopViewModel(application: Application) : AndroidViewModel(application) {
    private val dao: PlayerDao
    var repository = PlayerRepository(application)
    lateinit var currentPlayerCurrency : LiveData<Int>

    private val itemDao: ShopItemDao
    val shopRepository: ShopItemRepository
    val allItems: LiveData<List<ShopItem>>

    init {
        val database = PlayerInventoryDatabase.getDatabase(application)
        dao = database.playerDao()
        currentPlayerCurrency = repository.getPlayerCurrency().asLiveData()

        val shopDatabase = ShopItemDatabase.getDatabase(application)
        itemDao = shopDatabase.shopItemDao()
        shopRepository = ShopItemRepository(itemDao, dao)
        allItems = shopRepository.allItems
    }

    fun setPlayerCurrency(newCurrency: Int) {viewModelScope.launch(Dispatchers.IO) {repository.setPlayerCurrency(newCurrency)}}

    fun insertPlayer(value: Player) { InsertAsyncTask(dao).execute(value) }

    fun delete(value: Player) { dao.delete(value) }

    fun deleteTable() { DeleteTableAsyncTask(dao).execute() }
    fun insertItem(shopItem: ShopItem) {
        viewModelScope.launch(Dispatchers.IO) {
            if (shopRepository.getItemByName(shopItem.name).name == "") {
                shopRepository.insert(shopItem)
            }
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

    fun updateItemQuantity(name: String, quantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            shopRepository.updateQuantity(name, quantity)
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

