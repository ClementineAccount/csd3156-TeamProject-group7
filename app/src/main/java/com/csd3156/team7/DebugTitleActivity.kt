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

class DebugTitleActivity : AppCompatActivity() {


    private lateinit var musicService: MusicService
    private var isBound = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_debug_title)

        Log.d("DebugTitleActivity", "onCreate")
        Log.d("MusicService", "DebugTitleActivity->onCreate")
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
            //startService(intent)
            Log.d("MusicService", "DebugTitleActivity->bindService")
            Log.d("DebugTitleActivity", "bindService")

        }


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

//        findViewById<Button>(R.id.buttonShop)
//            .setOnClickListener {
//                Log.d("BUTTONS", "User tapped the buttonShop")
//                openShopScene()
//        }

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

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            musicService?.playMusic(R.raw.background_music_0)
            Log.d("MusicService", "DebugTitleActivity->onServiceConnected")

            Log.d("DebugTitleActivity", "onServiceConnected")

        }

        override fun onServiceDisconnected(arg0: ComponentName?) {
            Log.d("MusicService", "DebugTitleActivity->onServiceDisconnected")

            Log.d("DebugTitleActivity", "onServiceDisconnected")
            isBound = false

        }
    }



    override fun onDestroy() {
        super.onDestroy()
        Log.d("MusicService", "DebugTitleActivity->onDestroy")
        Log.d("DebugTitleActivity", "onDestroy")

        if (isBound) {
            Log.d("MusicService", "DebugTitleActivity->unbindService")

            Log.d("DebugTitleActivity", "unbindService")
            //musicService?.stopMusic()
            unbindService(connection)

            isBound = false
        }
    }

    override fun onPause() {
        super.onPause()
        if (isBound) {
            musicService.pauseMusic() // Assuming you have a method like this in your service
        }
    }

    override fun onResume() {
        super.onResume()
        if (isBound) {
            musicService.playMusic(R.raw.background_music_0) // Assuming you have a method like this in your service
        }
    }

}