package com.csd3156.team7


import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.provider.Settings
import android.util.Log


//Calling this in DebugTitleActivity for testing
public class FarmService : Service() {

    private var localCount : Int = 0
    private var handler: Handler? = null
    private var incrementTask: Runnable? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startIncrementTask()
        return START_STICKY
    }

    private fun startIncrementTask() {
        handler = Handler()
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

    override fun onDestroy() {
        stopIncrementTask()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}