package com.csd3156.team7


import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.ar.core.examples.kotlin.helloar.R

class MusicService : Service() {

    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaPlayer?.start()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        //mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun playMusic(trackResId: Int) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        Log.d("MusicService", "Attempting to play music")

        mediaPlayer = MediaPlayer.create(this,trackResId).apply {
            isLooping = true
            setVolume(1f, 1f)
            start()
            Log.d("MusicService", "Music playback started")
        }


        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer?.start()
            Log.d("MusicService", "Music playback started")
        }
        else
        {
            Log.d("MusicService", "Music is already playing")
        }
    }

    fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()

        }
    }

    fun stopMusic() {

            mediaPlayer?.stop()
            mediaPlayer?.release() // Release resources
            mediaPlayer = null // Ensure the MediaPlayer is recreated next time

    }






        // Add any additional methods here to control playback, such as play, pause, etc.
}