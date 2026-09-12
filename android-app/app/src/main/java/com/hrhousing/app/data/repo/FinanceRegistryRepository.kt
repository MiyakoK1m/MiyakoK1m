package com.hrhousing.app.data.repo

import com.hrhousing.app.data.db.dao.FinanceRegistryDao
import com.hrhousing.app.data.db.entity.FinanceRegistryEntryEntity
import com.hrhousing.app.data.net.ApiClient
import com.hrhousing.app.data.net.toFinanceRegistryEntity
import com.hrhousing.app.data.net.toJson
import com.hrhousing.app.data.prefs.ServerSettings
import kotlinx.coroutines.flow.Flow

class FinanceRegistryRepository(private val dao: FinanceRegistryDao, private val serverSettings: ServerSettings) {
    fun observeAll(): Flow<List<FinanceRegistryEntryEntity>> = dao.observeAll()

    private fun base(): String? = serverSettings.baseUrl.value.ifBlank { null }

    suspend fun refresh() {
        val url = base() ?: return
        val remote = runCatching { ApiClient.getArray(url, "/api/finance-registry") }.getOrNull() ?: return
        dao.clear()
        dao.insertAll((0 until remote.length()).map { remote.getJSONObject(it).toFinanceRegistryEntity() })
    }

    suspend fun upsert(entity: FinanceRegistryEntryEntity): Long {
        val url = base()
        if (url != null) {
            val result = runCatching { ApiClient.postObject(url, "/api/finance-registry", entity.toJson()) }.getOrNull()
            if (result != null) return dao.upsert(result.toFinanceRegistryEntity())
        }
        return dao.upsert(entity)
    }

    suspend fun delete(entity: FinanceRegistryEntryEntity) {
        val url = base()
        if (url != null && entity.id != 0L) runCatching { ApiClient.delete(url, "/api/finance-registry/${entity.id}") }
        dao.delete(entity)
    }

    suspend fun insertAll(entities: List<FinanceRegistryEntryEntity>) = dao.insertAll(entities)
    suspend fun clear() = dao.clear()
}
