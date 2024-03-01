package com.csd3156.team7

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.ar.core.examples.kotlin.helloar.NFCActivity
import com.google.ar.core.examples.kotlin.helloar.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DebugTestFarm : AppCompatActivity(), CoroutineScope by MainScope() {

    lateinit var playerViewModel: PlayerInventoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_test_farm)
        playerViewModel = ViewModelProvider(this)[PlayerInventoryViewModel::class.java]

        runBlocking {
            val job = launch {
                playerViewModel.farmRepository.delete()
                addFarmDefault()
            }
            job.join()
        }

        val addCubeFarmButton = findViewById<Button>(R.id.debugAddCubeFarm)
        addCubeFarmButton.setOnClickListener {
            runBlocking {
                val job = launch {
                    playerViewModel.deleteAllFarm()
                }
                job.join()
            }
        }

        startFarmActivity()

        findViewById<Button>(R.id.debugGoShopButton)
            .setOnClickListener {
                Log.d("BUTTONS", "User tapped the buttonShop")
                openShopScene()
            }

        val openMapButton = findViewById<Button>(R.id.debugOpenMapButton)
        openMapButton.setOnClickListener {
            Log.d("BUTTONS", "User tapped the Open Map button")
            openMapActivity()
        }
    }

    public fun addFarmDefault() {
        //1 cube every 3 seconds
        val newFarm = FarmItem(name = "My Pyramid Test Farm",
            lat = 37.4220936,
            long = 22.083922,
            alt = 0.0,
            qx_set = 0.0f,
            qy_set = 0.0f,
            qz_set = 0.0f,
            qw_set = 0.0f,
            growthTimeSet = 5.0f,
            shape = "Pyramid",
            rate = 31.0f)

        runBlocking {
            val job = launch {
                addFarm(newFarm)
            }
            job.join()
            printAllFarmItem(playerViewModel.allFarm)
        }
    }


    // Same as from HelloArActivity
    public suspend fun addFarm(newFarm : FarmItem) : Long {
        val generatedUid = playerViewModel.insert(newFarm)
        printAllFarmItem(playerViewModel.allFarm)
        return generatedUid
    }

    public fun printAllFarmItem(allEntity: LiveData<List<FarmItem>>) {
        runOnUiThread {
            var farmList: MutableList<FarmItem> = mutableListOf()
            allEntity.observe(this, Observer { farmList ->
                // The observer will be notified when the LiveData changes

                // Check if the list is not null and not empty before looping
                if (farmList != null && farmList.isNotEmpty()) {
                    // Loop through the FourDigit objects in the list
                    for (farm in farmList) {
                        //TODO: I will add the relevant data later oh my gosh
                        Log.d("DEBUG TEST FARM", "Farm ID: ${farm.uid}")
                        Log.d("DEBUG TEST FARM", "Farm Growth Time Set: ${farm.growthTime}")
                        Log.d("DEBUG TEST FARM", "Farm Growth Rate: ${farm.growthRate}")
                        Log.d("DEBUG TEST FARM", "Farm Location - Latitude: ${farm.lat}, longtitude: ${farm.long}, atude: ${farm.alt}")
                        //farmList.add(newEntity)
                    }
                }
            })
        }
    }

    fun startFarmActivity()
    {
        Log.d("DEBUG TEST FARM", "startFarmActivity() function called")

        // starting the service
        startService(Intent(this, FarmService::class.java))
    }

    fun openShopScene()
    {
        Log.d("DEBUG TITLE", "openShopScene() function called")
        val intent = Intent(this, ShopActivity::class.java)
        startActivity(intent)
    }

    fun openMapActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }
}