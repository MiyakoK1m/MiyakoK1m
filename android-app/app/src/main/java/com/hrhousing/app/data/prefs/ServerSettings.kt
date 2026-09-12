package com.hrhousing.app.data.prefs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SERVER_URL_KEY = "server_base_url"

/**
 * The web companion's base URL (e.g. `https://hr-housing-web.onrender.com`), or blank when the
 * app should stay fully local/offline. Every repository checks this before touching the network.
 */
class ServerSettings(private val jsonPrefStore: JsonPrefStore, scope: CoroutineScope) {
    val baseUrl: StateFlow<String> = jsonPrefStore.observeRaw(SERVER_URL_KEY)
        .map { it ?: "" }
        .stateIn(scope, SharingStarted.Eagerly, "")

    fun update(scope: CoroutineScope, url: String) {
        scope.launch { jsonPrefStore.writeRaw(SERVER_URL_KEY, url.trim()) }
    }

    fun isConfigured(): Boolean = baseUrl.value.isNotBlank()
}
