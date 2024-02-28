package com.csd3156.team7

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.examples.kotlin.helloar.HelloArActivity
import com.google.ar.core.examples.kotlin.helloar.R

class DebugTitleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_title)

        findViewById<Button>(R.id.buttonAR)
            .setOnClickListener {
                Log.d("BUTTONS", "User tapped the buttonAR")
                openARScene()
            }

        findViewById<Button>(R.id.buttonMap)
            .setOnClickListener {
                Log.d("BUTTONS", "User tapped the buttonMap")
                openMapScene()
            }

        findViewById<Button>(R.id.buttonShop)
            .setOnClickListener {
                Log.d("BUTTONS", "User tapped the buttonShop")
                openShopScene()
        }

        //startFarmActivity()

    }

    fun startFarmActivity()
    {
        Log.d("DEBUG TITLE", "startFarmActivity() function called")

        // starting the service
        startService(Intent(this, FarmService::class.java))
    }

    fun openARScene()
    {
        Log.d("DEBUG TITLE", "openARScene() function called")
        val intent = Intent(this, HelloArActivity::class.java)
        startActivity(intent)
    }

    fun openMapScene()
    {
        Log.d("DEBUG TITLE", "openMapScene() function called")
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    fun openShopScene()
    {
        Log.d("DEBUG TITLE", "openShopScene() function called")
        val intent = Intent(this, ShopActivity::class.java)
        startActivity(intent)
    }
}