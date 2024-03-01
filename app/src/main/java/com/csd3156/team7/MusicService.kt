package com.csd3156.team7


import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.ar.core.examples.kotlin.helloar.R

/**
 * Service class responsible for managing background music playback.
 */
class MusicService : Service() {

    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Binder class to provide a reference to the MusicService.
     */
    inner class MusicBinder : Binder() {


        fun getService(): MusicService = this@MusicService
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

    /**
     * Start playing the specified music track.
     *
     * @param trackResId The resource ID of the music track to play.
     */
    fun playMusic(trackResId: Int) {
        stopMusic()
        Log.d("MusicService", "Attempting to play $trackResId")

        mediaPlayer = MediaPlayer.create(this,trackResId).apply {
            isLooping = true
            setVolume(0.25f, 0.25f)
            start()
            Log.d("MusicService", "$trackResId playback started $trackResId")
        }


//        if (!mediaPlayer!!.isPlaying) {
//            mediaPlayer?.start()
//            Log.d("MusicService", "Music playback started")
//        }
//        else
//        {
//            Log.d("MusicService", "Music is already playing")
//        }
    }
    /**
     * Pause the currently playing music.
     */
    fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()

        }
    }

    /**
     * Stop the currently playing music.
     */
    fun stopMusic() {

        Log.d("MusicService", "Attempting to stop music")

        mediaPlayer?.stop()
        mediaPlayer?.release() // Release resources
        mediaPlayer = null // Ensure the MediaPlayer is recreated next time

    }






        // Add any additional methods here to control playback, such as play, pause, etc.
}