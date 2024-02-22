package com.csd3156.team7

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException


class PlayerRepository(private val context: Context) {
    val Context.dataStore by preferencesDataStore(name = "PlayerCurrency")

    companion object {
        private val PLAYER_CURRENCY = intPreferencesKey("PlayerCurrency")
    }

    fun getPlayerCurrency(): Flow<Int> {
        return context.dataStore.data.catch { exception ->
            if (exception is IOException) {
                Log.e("Exception", "Error - getPlayerCurrency")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val playerCurrency = preferences[PLAYER_CURRENCY] ?: 0
            playerCurrency
        }
    }

    suspend fun setPlayerCurrency(newCurrency : Int) {
        context.dataStore.edit { preferences -> preferences[PLAYER_CURRENCY] = newCurrency
        }
    }
}