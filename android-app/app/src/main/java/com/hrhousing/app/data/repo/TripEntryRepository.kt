package com.hrhousing.app.data.repo

import com.hrhousing.app.data.db.dao.TripEntryDao
import com.hrhousing.app.data.db.entity.TripEntryEntity
import com.hrhousing.app.data.net.ApiClient
import com.hrhousing.app.data.net.toJson
import com.hrhousing.app.data.net.toTripEntryEntity
import com.hrhousing.app.data.prefs.ServerSettings
import kotlinx.coroutines.flow.Flow

class TripEntryRepository(private val dao: TripEntryDao, private val serverSettings: ServerSettings) {
    fun observeAll(): Flow<List<TripEntryEntity>> = dao.observeAll()
    suspend fun getAll(): List<TripEntryEntity> = dao.getAll()
    suspend fun getByCity(city: String): List<TripEntryEntity> = dao.getByCity(city)

    private fun base(): String? = serverSettings.baseUrl.value.ifBlank { null }

    suspend fun refresh() {
        val url = base() ?: return
        val remote = runCatching { ApiClient.getArray(url, "/api/trip-entries") }.getOrNull() ?: return
        dao.clear()
        dao.insertAll((0 until remote.length()).map { remote.getJSONObject(it).toTripEntryEntity() })
    }

    suspend fun upsert(entity: TripEntryEntity): Long {
        val url = base()
        if (url != null) {
            val result = runCatching {
                if (entity.id == 0L) ApiClient.postObject(url, "/api/trip-entries", entity.toJson())
                else ApiClient.putObject(url, "/api/trip-entries/${entity.id}", entity.toJson())
            }.getOrNull()
            if (result != null) return dao.upsert(result.toTripEntryEntity())
        }
        return dao.upsert(entity)
    }

    suspend fun update(entity: TripEntryEntity) { upsert(entity) }

    suspend fun delete(entity: TripEntryEntity) {
        val url = base()
        if (url != null && entity.id != 0L) runCatching { ApiClient.delete(url, "/api/trip-entries/${entity.id}") }
        dao.delete(entity)
    }

    suspend fun insertAll(entities: List<TripEntryEntity>) = dao.insertAll(entities)
    suspend fun clear() = dao.clear()
}
