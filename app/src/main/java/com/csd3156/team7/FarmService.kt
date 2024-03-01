package com.csd3156.team7


import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


//Calling this in DebugTitleActivity for testing
// Declares a service class for the farm feature in the app. This service is responsible for background operations related to farm management.
public class FarmService : Service() {

    // A local counter variable, potentially for tracking instances or operations within the service.
    private var localCount : Int = 0

    // Handler to post tasks to the main thread's message queue.
    private var handler: Handler? = null

    // Runnable task for incrementing a local counter, not directly related to farm functionality.
    private var incrementTask: Runnable? = null

    // Runnable task for periodically updating farm data.
    private var incrementTaskFarm: Runnable? = null

    // Late-initialized properties for accessing database and repository layers.
    lateinit var farmRepository : FarmListRepository
    lateinit var farmDao: FarmDao
    lateinit var playerDao : PlayerDao
    lateinit var shopRepository : ShopItemRepository
    lateinit var itemDao: ShopItemDao


    private var pryamidGrowthTimeSeconds : Long = 1L
    private var pryamidGrowthRate : Int = 5

    private var cubeGrowthTimeSeconds : Long = 1L
    private var cubeGrowthRate : Int = 10

    private var sphereGrowthTimeSeconds : Long = 1L
    private var sphereGrowthRate : Int = 15

    override fun onCreate() {
        super.onCreate()
        // Retrieves instances of the database and initializes DAOs and repositories.
        val database = PlayerInventoryDatabase.getDatabase(application)
        farmDao = database.farmDao()
        farmRepository = FarmListRepository(farmDao)

        playerDao = database.playerDao()

        val shopDatabase = ShopItemDatabase.getDatabase(application)
        itemDao = shopDatabase.shopItemDao()
        shopRepository = ShopItemRepository(itemDao, playerDao)

    }

    // Handles service start commands. Currently, it only starts the farm increment task.
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //startIncrementTask()
        startIncrementFarm()
        return START_STICKY
    }


    // Defines a task for periodically updating farm data, specifically the quantity of "Pyramid" items.
    private fun startIncrementFarm(){
        handler = Handler(Looper.getMainLooper())
        incrementTaskFarm = object : Runnable {
            override fun run() {
                // Task to fetch, update, and persist "Pyramid" item data.
                    GlobalScope.launch(Dispatchers.IO)
                    {

                        if (MapsActivity.isFarmWithin)
                        {
                            if (MapsActivity.farmShape == "Pyramid")
                            {
                                var pyramidItem = shopRepository.getItemByName("Pyramid")

                                //TODO: Increment it here
                                //TODO: Increment by formula that takes into account the farm
                                //TODO: Set it back
                                var updateCount = shopRepository.getItemQuantity(pyramidItem.itemId)
                                updateCount += pryamidGrowthRate

                                //Log.d("FarmService", "pyramidItem Quantity: ${updateCount}")
                                shopRepository.updateQuantity(pyramidItem.itemId, updateCount)
                                pyramidItem = shopRepository.getItemByName("Pyramid")
                            }
                            else if (MapsActivity.farmShape == "Cube")
                            {
                                var pyramidItem = shopRepository.getItemByName("Cube")

                                //TODO: Increment it here
                                //TODO: Increment by formula that takes into account the farm
                                //TODO: Set it back
                                var updateCount = shopRepository.getItemQuantity(pyramidItem.itemId)
                                updateCount += pryamidGrowthRate

                                //Log.d("FarmService", "pyramidItem Quantity: ${updateCount}")
                                shopRepository.updateQuantity(pyramidItem.itemId, updateCount)
                                pyramidItem = shopRepository.getItemByName("Cube")
                            }
                            else if (MapsActivity.farmShape == "Sphere") {
                                var pyramidItem = shopRepository.getItemByName("Sphere")

                                //TODO: Increment it here
                                //TODO: Increment by formula that takes into account the farm
                                //TODO: Set it back
                                var updateCount = shopRepository.getItemQuantity(pyramidItem.itemId)
                                updateCount += pryamidGrowthRate

                                //Log.d("FarmService", "pyramidItem Quantity: ${updateCount}")
                                shopRepository.updateQuantity(pyramidItem.itemId, updateCount)
                                pyramidItem = shopRepository.getItemByName("Sphere")
                            }
                        }
                    }

                // Schedules the task to run again after a specified time interval.
                handler?.postDelayed(this, pryamidGrowthTimeSeconds * 1000)
            }
        }
        // Start the task immediately
        handler?.post(incrementTaskFarm!!)
    }

    // A method for starting a task that increments a local counter. Currently unused.
    private fun startIncrementTask() {
        handler = Handler(Looper.getMainLooper())
        incrementTask = object : Runnable {
            override fun run() {
                // Increment localCount
                localCount++
                // Schedule the task again after 3 seconds (adjust the delay as needed)
                handler?.postDelayed(this, 3000) // 3000 milliseconds = 3 seconds
                Log.d("FarmService", "localCount: $localCount")
            }
        }
        // Start the task immediately
        handler?.post(incrementTask!!)
    }

    // Stops the local counter increment task. Currently unused.
    private fun stopIncrementTask() {
        handler?.removeCallbacks(incrementTask!!)
    }

    // Stops the farm update task.
    private fun stopIncrementTaskFarm() {
        handler?.removeCallbacks(incrementTaskFarm!!)
    }

    // Cleans up when the service is destroyed, specifically stopping the farm update task.
    override fun onDestroy() {
        //stopIncrementTask()
        stopIncrementTaskFarm()
        super.onDestroy()
    }

    // Returns null as this service does not support binding.
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}