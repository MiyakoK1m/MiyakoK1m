package com.hrhousing.app.data.repo

import com.hrhousing.app.data.db.dao.FinanceRegistryDao
import com.hrhousing.app.data.db.entity.FinanceRegistryEntryEntity
import kotlinx.coroutines.flow.Flow

class FinanceRegistryRepository(private val dao: FinanceRegistryDao) {
    fun observeAll(): Flow<List<FinanceRegistryEntryEntity>> = dao.observeAll()
    suspend fun upsert(entity: FinanceRegistryEntryEntity) = dao.upsert(entity)
    suspend fun delete(entity: FinanceRegistryEntryEntity) = dao.delete(entity)
    suspend fun insertAll(entities: List<FinanceRegistryEntryEntity>) = dao.insertAll(entities)
    suspend fun clear() = dao.clear()
}
