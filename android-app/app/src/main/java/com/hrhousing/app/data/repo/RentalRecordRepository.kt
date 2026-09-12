package com.hrhousing.app.data.repo

import com.hrhousing.app.data.db.dao.RentalRecordDao
import com.hrhousing.app.data.db.entity.RentalRecordEntity
import com.hrhousing.app.data.net.ApiClient
import com.hrhousing.app.data.net.toJson
import com.hrhousing.app.data.net.toRentalRecordEntity
import com.hrhousing.app.data.prefs.ServerSettings
import kotlinx.coroutines.flow.Flow

class RentalRecordRepository(private val dao: RentalRecordDao, private val serverSettings: ServerSettings) {
    fun observeAll(): Flow<List<RentalRecordEntity>> = dao.observeAll()
    suspend fun getAll(): List<RentalRecordEntity> = dao.getAll()
    suspend fun getById(id: Long): RentalRecordEntity? = dao.getById(id)

    private fun base(): String? = serverSettings.baseUrl.value.ifBlank { null }

    suspend fun refresh() {
        val url = base() ?: return
        val remote = runCatching { ApiClient.getArray(url, "/api/rental-records") }.getOrNull() ?: return
        dao.clear()
        dao.insertAll((0 until remote.length()).map { remote.getJSONObject(it).toRentalRecordEntity() })
    }

    suspend fun upsert(entity: RentalRecordEntity): Long {
        val url = base()
        if (url != null) {
            val result = runCatching {
                if (entity.id == 0L) ApiClient.postObject(url, "/api/rental-records", entity.toJson())
                else ApiClient.putObject(url, "/api/rental-records/${entity.id}", entity.toJson())
            }.getOrNull()
            if (result != null) return dao.upsert(result.toRentalRecordEntity())
        }
        return dao.upsert(entity)
    }

    suspend fun update(entity: RentalRecordEntity) { upsert(entity) }

    suspend fun delete(entity: RentalRecordEntity) {
        val url = base()
        if (url != null && entity.id != 0L) runCatching { ApiClient.delete(url, "/api/rental-records/${entity.id}") }
        dao.delete(entity)
    }

    suspend fun insertAll(entities: List<RentalRecordEntity>) = dao.insertAll(entities)
    suspend fun clear() = dao.clear()
}
