package com.hrhousing.app.data.repo

import com.hrhousing.app.data.db.dao.LandlordDao
import com.hrhousing.app.data.db.entity.LandlordEntity
import com.hrhousing.app.data.net.ApiClient
import com.hrhousing.app.data.net.toJson
import com.hrhousing.app.data.net.toLandlordEntity
import com.hrhousing.app.data.prefs.ServerSettings
import kotlinx.coroutines.flow.Flow

class LandlordRepository(private val dao: LandlordDao, private val serverSettings: ServerSettings) {
    fun observeAll(): Flow<List<LandlordEntity>> = dao.observeAll()
    fun observeByCity(city: String): Flow<List<LandlordEntity>> = dao.observeByCity(city)

    private fun base(): String? = serverSettings.baseUrl.value.ifBlank { null }

    suspend fun refresh() {
        val url = base() ?: return
        val remote = runCatching { ApiClient.getArray(url, "/api/landlords") }.getOrNull() ?: return
        dao.clear()
        dao.insertAll((0 until remote.length()).map { remote.getJSONObject(it).toLandlordEntity() })
    }

    suspend fun upsert(entity: LandlordEntity): Long {
        val url = base()
        if (url != null) {
            val result = runCatching {
                if (entity.id == 0L) ApiClient.postObject(url, "/api/landlords", entity.toJson())
                else ApiClient.putObject(url, "/api/landlords/${entity.id}", entity.toJson())
            }.getOrNull()
            if (result != null) return dao.upsert(result.toLandlordEntity())
        }
        return dao.upsert(entity)
    }

    suspend fun update(entity: LandlordEntity) { upsert(entity) }

    suspend fun delete(entity: LandlordEntity) {
        val url = base()
        if (url != null && entity.id != 0L) runCatching { ApiClient.delete(url, "/api/landlords/${entity.id}") }
        dao.delete(entity)
    }

    suspend fun insertAll(entities: List<LandlordEntity>) = dao.insertAll(entities)
    suspend fun clear() = dao.clear()
}
