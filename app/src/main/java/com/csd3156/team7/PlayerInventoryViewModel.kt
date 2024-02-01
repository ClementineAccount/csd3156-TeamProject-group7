package com.csd3156.team7

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayerInventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao: PlayerDao
//    val listShopItem: LiveData<List<ShopItem>>

    var repository = PlayerRepository(application)
    var currentPlayerCurrency : LiveData<Int>



    init {
        val database = PlayerInventoryDatabase.getDatabase(application)
        dao = database.playerDao()
//        listShopItem = dao.getAllItems()
        currentPlayerCurrency = dao.getPlayerCurrency()
    }

    fun setPlayerCurrency(newCurrency: Int) {
        viewModelScope.launch(Dispatchers.IO) {repository.setPlayerCurrency(newCurrency) }
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

//     If you need to pass dependencies, consider using a Factory
//     Define ViewModel factory in a companion object
    companion object {
        val Factory : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()

                return PlayerInventoryViewModel(
                    application) as T
            }
        }
    }
}
