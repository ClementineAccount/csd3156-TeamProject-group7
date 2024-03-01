package com.csd3156.team7

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FarmListRepository(private val farmDao: FarmDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val alLFarms: Flow<List<FarmItem>> = farmDao.getAll()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.




    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(farm: FarmItem) {
        farmDao.insert(farm)
    }

    suspend fun insertFarmItemAndGetUid(farm: FarmItem) : Long {
        return withContext(Dispatchers.IO) {
            farmDao.insert(farm)
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete()
    {
        farmDao.deleteAllFarm()
    }

    fun GetFirstFarm(): LiveData<FarmItem> {
        return farmDao.getFirstFarm()
    }
}