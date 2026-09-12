package com.hrhousing.app.data.repo

import com.hrhousing.app.data.db.dao.ResidentEmployeeDao
import com.hrhousing.app.data.db.entity.ResidentEmployeeEntity
import com.hrhousing.app.data.net.ApiClient
import com.hrhousing.app.data.net.toJson
import com.hrhousing.app.data.net.toResidentEntity
import com.hrhousing.app.data.prefs.ServerSettings
import kotlinx.coroutines.flow.Flow

class ResidentEmployeeRepository(private val dao: ResidentEmployeeDao, private val serverSettings: ServerSettings) {
    fun observeAll(): Flow<List<ResidentEmployeeEntity>> = dao.observeAll()
    suspend fun getAll(): List<ResidentEmployeeEntity> = dao.getAll()

    private fun base(): String? = serverSettings.baseUrl.value.ifBlank { null }

    suspend fun refresh() {
        val url = base() ?: return
        val remote = runCatching { ApiClient.getArray(url, "/api/residents") }.getOrNull() ?: return
        dao.clear()
        dao.insertAll((0 until remote.length()).map { remote.getJSONObject(it).toResidentEntity() })
    }

    suspend fun upsert(entity: ResidentEmployeeEntity): Long {
        val url = base()
        if (url != null) {
            val result = runCatching {
                if (entity.id == 0L) ApiClient.postObject(url, "/api/residents", entity.toJson())
                else ApiClient.putObject(url, "/api/residents/${entity.id}", entity.toJson())
            }.getOrNull()
            if (result != null) return dao.upsert(result.toResidentEntity())
        }
        return dao.upsert(entity)
    }

    suspend fun update(entity: ResidentEmployeeEntity) { upsert(entity) }

    suspend fun delete(entity: ResidentEmployeeEntity) {
        val url = base()
        if (url != null && entity.id != 0L) runCatching { ApiClient.delete(url, "/api/residents/${entity.id}") }
        dao.delete(entity)
    }

    suspend fun insertAll(entities: List<ResidentEmployeeEntity>) = dao.insertAll(entities)
    suspend fun clear() = dao.clear()
}
