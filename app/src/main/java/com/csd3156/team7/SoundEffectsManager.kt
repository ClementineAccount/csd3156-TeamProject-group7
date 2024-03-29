package com.csd3156.team7

import android.content.Context
import android.media.SoundPool
import com.google.ar.core.examples.kotlin.helloar.R
import kotlin.random.Random
class SoundEffectsManager(context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(10).build() // Adjust max streams as needed

    private val soundEffectsMap: MutableMap<String, Int> = mutableMapOf()
//    private var soundEffectsMap: Map<String, Int> = mapOf(
//        "drop1" to soundPool.load(context, R.raw.drop1, 1),
//        //"notification" to soundPool.load(context, R.raw.notification_sound, 1)
//        // Add more sound effects as needed
//    )

    var collectSoundID : Int = 0

    init {
        // Load sound effects into the map
        soundEffectsMap["drop1"] = soundPool.load(context, R.raw.drop1, 1)
        soundEffectsMap["drop2"] = soundPool.load(context, R.raw.drop2, 1)
        soundEffectsMap["drop3"] = soundPool.load(context, R.raw.drop3, 1)
        soundEffectsMap["drop4"] = soundPool.load(context, R.raw.drop4, 1)
        soundEffectsMap["drop5"] = soundPool.load(context, R.raw.drop5, 1)


        collectSoundID = soundPool.load(context, R.raw.collect, 1)
        // Add more sound effects as needed
    }

    fun playSound(effectName: String) {
        soundEffectsMap[effectName]?.let { soundId ->
            soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }

    fun playCollectSound()
    {
        soundPool.play(collectSoundID, 1.0f, 1.0f, 0, 0, 1.0f)
    }


    fun playRandomSound() {
        val soundIds = soundEffectsMap.values.toList()
        if (soundIds.isNotEmpty()) {
            // Randomly select a sound effect to play
            val randomSoundId = soundIds[Random.nextInt(soundIds.size)]
            soundPool.play(randomSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }
}