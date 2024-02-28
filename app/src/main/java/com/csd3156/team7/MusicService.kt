package com.csd3156.team7


import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.ar.core.examples.kotlin.helloar.R

class MusicService : Service() {

    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null

    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music).apply {
            isLooping = true
            setVolume(1f, 1f)
        }
        mediaPlayer?.setOnErrorListener { mp, what, extra ->
            Log.e("MediaPlayer Error", "What: $what, Extra: $extra")
            true // True if the method handled the error
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaPlayer?.start()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun playMusic() {
        Log.d("MusicService", "Attempting to play music")
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.background_music).apply {
                isLooping = true
                setVolume(1f, 1f)
            }
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



    // Add any additional methods here to control playback, such as play, pause, etc.
}