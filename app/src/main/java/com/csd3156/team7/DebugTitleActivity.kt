package com.csd3156.team7

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.examples.kotlin.helloar.HelloArActivity
import com.google.ar.core.examples.kotlin.helloar.R

// A class that extends AppCompatActivity to manage the DebugTitleActivity UI and functionality.
class DebugTitleActivity : AppCompatActivity() {


    // Declaration of MusicService variable for music playback, not initialized yet.
    private lateinit var musicService: MusicService
    // A flag to track whether the service is bound.
    private var isBound = false

    // The onCreate method is called when the activity is starting.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Setting the user interface layout for this Activity.
        setContentView(R.layout.activity_debug_title)

        // Logging for debugging purposes.
        Log.d("DebugTitleActivity", "onCreate")
        Log.d("MusicService", "DebugTitleActivity->onCreate")

        // Binding to the MusicService to play background music.
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
            // Logging for debugging purposes.
            Log.d("MusicService", "DebugTitleActivity->bindService")
            Log.d("DebugTitleActivity", "bindService")

        }

        // Setting up onClickListener for the AR button to open AR scene.
        findViewById<Button>(R.id.buttonAR)
            .setOnClickListener {
                Log.d("BUTTONS", "User tapped the buttonAR")
                openARScene()
            }

        // Setting up onClickListener for the Map button to open Map scene.
        findViewById<Button>(R.id.buttonMap)
            .setOnClickListener {
                Log.d("BUTTONS", "User tapped the buttonMap")
                openMapScene()
            }

        // Setting up onClickListener for the Shop button to open Shop scene.
        findViewById<Button>(R.id.buttonShop)
            .setOnClickListener {
                Log.d("BUTTONS", "User tapped the buttonShop")
                openShopScene()
        }

        //startFarmActivity()

    }

    // Method to start the FarmService explicitly.
    fun startFarmActivity()
    {
        Log.d("DEBUG TITLE", "startFarmActivity() function called")

        // starting the service
        startService(Intent(this, FarmService::class.java))
    }

    // Method to open the AR scene.
    fun openARScene()
    {
        Log.d("DEBUG TITLE", "openARScene() function called")
        val intent = Intent(this, HelloArActivity::class.java)
        startActivity(intent)
    }

    // Method to open the Map scene.
    fun openMapScene()
    {
        Log.d("DEBUG TITLE", "openMapScene() function called")
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    // Method to open the Shop scene.
    fun openShopScene()
    {
        Log.d("DEBUG TITLE", "openShopScene() function called")
        val intent = Intent(this, ShopActivity::class.java)
        startActivity(intent)
    }

    // Defines callbacks for service binding, passed to bindService().
    private val connection = object : ServiceConnection {

        // Called when a connection to the Service has been established, with the IBinder of the communication channel to the Service.
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            // Plays music using the bound service.
            musicService?.playMusic(R.raw.background_music_0)
            Log.d("MusicService", "DebugTitleActivity->onServiceConnected")

            Log.d("DebugTitleActivity", "onServiceConnected")

        }

        // Called when a connection to the Service has been lost.
        override fun onServiceDisconnected(arg0: ComponentName?) {
            Log.d("MusicService", "DebugTitleActivity->onServiceDisconnected")

            Log.d("DebugTitleActivity", "onServiceDisconnected")
            isBound = false

        }
    }

    // Overridden onDestroy method to unbind from the service when the activity is destroyed.
    override fun onDestroy() {
        super.onDestroy()
        Log.d("MusicService", "DebugTitleActivity->onDestroy")
        Log.d("DebugTitleActivity", "onDestroy")

        // Unbind from the service if the activity is bound to it.
        if (isBound) {
            Log.d("MusicService", "DebugTitleActivity->unbindService")

            Log.d("DebugTitleActivity", "unbindService")
            //musicService?.stopMusic()
            unbindService(connection)

            isBound = false
        }
    }

    // Overridden onPause method to pause music when the activity is paused.
    override fun onPause() {
        super.onPause()
        // Pauses music playback if the service is bound.
        if (isBound) {
            musicService.pauseMusic() // Assuming you have a method like this in your service
        }
    }

    // Overridden onResume method to resume music when the activity is resumed.
    override fun onResume() {
        super.onResume()
        // Resumes music playback if the service is bound.
        if (isBound) {
            musicService.playMusic(R.raw.background_music_0) // Assuming you have a method like this in your service
        }
    }

}