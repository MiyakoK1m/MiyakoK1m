package com.hrhousing.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "hr_housing_settings")

/** Small helper for storing one JSON string under a key in the shared DataStore. */
class JsonPrefStore(private val context: Context) {
    fun observeRaw(key: String): Flow<String?> {
        val prefKey = stringPreferencesKey(key)
        return context.dataStore.data.map { it[prefKey] }
    }

    suspend fun writeRaw(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        context.dataStore.edit { it[prefKey] = value }
    }
}
