package com.csd3156.team7

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PlayerInventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao: PlayerDao
    val listShopItem: LiveData<List<ShopItem>>

    var repository = PlayerRepository(application)
    var currentPlayerCurrency = repository.getPlayerCurrency()

    init {
        val database = PlayerInventoryDatabase.getDatabase(application)
        dao = database.playerDao()
        listShopItem = dao.getAllItems()
    }

    fun setPlayerCurrency(newCurrency: Int) {
        viewModelScope.launch {repository.setPlayerCurrency(newCurrency) }
    }

    fun insertPlayer(value: PlayerData) { InsertAsyncTask(dao).execute(value) }

    fun delete(value: PlayerData) { dao.delete(value) }

    fun deleteTable() { DeleteTableAsyncTask(dao).execute() }

    private class DeleteTableAsyncTask(private val dao: PlayerDao) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? { dao.deleteTable(); return null }
    }

    private class InsertAsyncTask(dao: PlayerDao) : AsyncTask<PlayerData, Void, Void>() {
        @Deprecated("Deprecated in Java", ReplaceWith("null"))
        public override fun doInBackground(vararg params: PlayerData): Void? {
//            dao.insert(params[0])
            return null
        }
    }
}