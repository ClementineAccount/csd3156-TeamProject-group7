package com.csd3156.team7

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Defines a repository class for managing farm data operations. This class abstracts access to multiple data sources.
class FarmListRepository(private val farmDao: FarmDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val alLFarms: Flow<List<FarmItem>> = farmDao.getAll()
    public var currentFarmShape : String = "Pyramid"

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.




    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(farm: FarmItem) {
        farmDao.insert(farm)
    }

    // Inserts a farm item into the database and returns its generated UID.
    // Explicitly specifies to run this operation on the IO dispatcher for database operations.
    suspend fun insertFarmItemAndGetUid(farm: FarmItem) : Long {
        return withContext(Dispatchers.IO) {
            farmDao.insert(farm)
        }
    }

    // Deletes all farm items from the database. Although Room executes suspend functions on a background thread,
    // the @WorkerThread annotation is used as a reminder to not call this method on the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete()
    {
        farmDao.deleteAllFarm()
    }

    // Retrieves the first farm item from the database. Returns LiveData, allowing the UI to observe changes.
    // LiveData is lifecycle-aware, ensuring the UI only updates when active.
    fun GetFirstFarm(): LiveData<FarmItem> {
        return farmDao.getFirstFarm()
    }
}