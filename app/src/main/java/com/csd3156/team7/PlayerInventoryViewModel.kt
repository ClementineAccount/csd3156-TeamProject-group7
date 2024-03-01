package com.csd3156.team7

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.asLiveData
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ViewModel for managing player inventory-related data
class PlayerInventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao: PlayerDao
    private val farmDao: FarmDao

    var repository = PlayerRepository(application)
    lateinit var farmRepository : FarmListRepository
    lateinit var currentPlayerCurrency : LiveData<Int>

    lateinit var allFarm :LiveData<List<FarmItem>>

    // Coroutine function to insert a farm item
    suspend fun insert(farm: FarmItem): Long {
        return farmRepository.insertFarmItemAndGetUid(farm)
    }

    // Coroutine function to delete all farm items
    fun deleteAllFarm() = viewModelScope.launch {
        farmRepository.delete()
    }

    // Coroutine function to get the first farm item
    fun getFirstFarm() = farmRepository.GetFirstFarm()

    // Initialize ViewModel
    init {
        val database = PlayerInventoryDatabase.getDatabase(application)
        dao = database.playerDao()
        farmDao = database.farmDao()
        farmRepository = FarmListRepository(farmDao)
        allFarm = farmRepository.alLFarms.asLiveData()
        currentPlayerCurrency = repository.getPlayerCurrency().asLiveData()
    }

    // Coroutine function to set player currency
    fun setPlayerCurrency(newCurrency: Int) {viewModelScope.launch(Dispatchers.IO) {repository.setPlayerCurrency(newCurrency)}}

    // Coroutine function to insert player
    fun insertPlayer(value: Player) { InsertAsyncTask(dao).execute(value) }

    // Coroutine function to delete all farm items associated with a player
    fun deleteAllFarm(value: Player) { dao.delete(value) }

    // Coroutine function to delete the player table
    fun deleteTable() { DeleteTableAsyncTask(dao).execute() }

    // AsyncTask to delete the player table
    private class DeleteTableAsyncTask(private val dao: PlayerDao) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? { dao.deleteTable(); return null }
    }

    // AsyncTask to insert player
    private class InsertAsyncTask(dao: PlayerDao) : AsyncTask<Player, Void, Void>() {
        @Deprecated("Deprecated in Java", ReplaceWith("null"))
        public override fun doInBackground(vararg params: Player): Void? {
//            dao.insert(params[0])
            return null
        }
    }

//     If you need to pass dependencies, consider using a Factory define ViewModel factory in a companion object
    // Companion object to provide a ViewModelFactory for dependency injection
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
                // Return an instance of PlayerInventoryViewModel
                return PlayerInventoryViewModel(
                    application) as T
            }
        }
    }
}

