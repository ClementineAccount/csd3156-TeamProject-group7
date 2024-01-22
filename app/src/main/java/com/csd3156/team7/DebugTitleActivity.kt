package com.csd3156.team7

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
}