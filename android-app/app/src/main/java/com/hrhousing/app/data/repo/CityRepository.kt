package com.hrhousing.app.data.repo

import com.hrhousing.app.data.db.dao.CityDao
import com.hrhousing.app.data.db.entity.CityEntity
import com.hrhousing.app.data.db.entity.DEFAULT_CITIES
import com.hrhousing.app.data.net.ApiClient
import com.hrhousing.app.data.net.toCityEntity
import com.hrhousing.app.data.net.toJson
import com.hrhousing.app.data.prefs.ServerSettings
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

class CityRepository(private val dao: CityDao, private val serverSettings: ServerSettings) {
    fun observeAll(): Flow<List<CityEntity>> = dao.observeAll()

    private fun base(): String? = serverSettings.baseUrl.value.ifBlank { null }

    suspend fun names(): List<String> = dao.getAll().map { it.name }

    suspend fun ensureSeeded() {
        if (base() == null && dao.getAll().isEmpty()) resetToDefault()
    }

    /** Pulls the server's copy into the local cache; a no-op when no server is configured. */
    suspend fun refresh() {
        val url = base() ?: return
        val remote = runCatching { ApiClient.getArray(url, "/api/cities") }.getOrNull() ?: return
        val entities = (0 until remote.length()).map { remote.getJSONObject(it).toCityEntity() }
        dao.clear()
        dao.insertAll(entities)
    }

    suspend fun add(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val url = base()
        if (url != null) {
            val created = runCatching { ApiClient.postObject(url, "/api/cities", JSONObject().put("name", trimmed)) }.getOrNull()
            if (created != null) { dao.insert(created.toCityEntity()); return }
        }
        dao.insert(CityEntity(name = trimmed))
    }

    suspend fun delete(city: CityEntity) {
        val url = base()
        if (url != null && city.id != 0L) runCatching { ApiClient.delete(url, "/api/cities/${city.id}") }
        dao.delete(city)
    }

    suspend fun resetToDefault() {
        val url = base()
        if (url != null) {
            val remote = runCatching { ApiClient.postForArray(url, "/api/cities-reset") }.getOrNull()
            if (remote != null) {
                dao.clear()
                dao.insertAll((0 until remote.length()).map { remote.getJSONObject(it).toCityEntity() })
                return
            }
        }
        dao.clear()
        dao.insertAll(DEFAULT_CITIES.map { CityEntity(name = it) })
    }
}
