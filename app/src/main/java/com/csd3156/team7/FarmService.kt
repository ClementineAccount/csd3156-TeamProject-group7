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
public class FarmService : Service() {

    private var localCount : Int = 0
    private var handler: Handler? = null
    private var incrementTask: Runnable? = null
    private var incrementTaskFarm: Runnable? = null

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
        val database = PlayerInventoryDatabase.getDatabase(application)
        farmDao = database.farmDao()
        farmRepository = FarmListRepository(farmDao)

        playerDao = database.playerDao()

        val shopDatabase = ShopItemDatabase.getDatabase(application)
        itemDao = shopDatabase.shopItemDao()
        shopRepository = ShopItemRepository(itemDao, playerDao)

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //startIncrementTask()
        startIncrementFarm()
        return START_STICKY
    }


    //TODO: This actual function
    private fun startIncrementFarm(){
        handler = Handler(Looper.getMainLooper())
        incrementTaskFarm = object : Runnable {
            override fun run() {
                //UPDATE PRYAMID
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
                handler?.postDelayed(this, pryamidGrowthTimeSeconds * 1000)
            }
        }
        // Start the task immediately
        handler?.post(incrementTaskFarm!!)
    }

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
    private fun stopIncrementTask() {
        handler?.removeCallbacks(incrementTask!!)
    }

    private fun stopIncrementTaskFarm() {
        handler?.removeCallbacks(incrementTaskFarm!!)
    }

    override fun onDestroy() {
        //stopIncrementTask()
        stopIncrementTaskFarm()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}