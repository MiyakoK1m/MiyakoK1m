package com.hrhousing.app.data.repo

import com.hrhousing.app.data.db.dao.RentalRecordDao
import com.hrhousing.app.data.db.entity.RentalRecordEntity
import kotlinx.coroutines.flow.Flow

class RentalRecordRepository(private val dao: RentalRecordDao) {
    fun observeAll(): Flow<List<RentalRecordEntity>> = dao.observeAll()
    suspend fun getAll(): List<RentalRecordEntity> = dao.getAll()
    suspend fun getById(id: Long): RentalRecordEntity? = dao.getById(id)
    suspend fun upsert(entity: RentalRecordEntity): Long = dao.upsert(entity)
    suspend fun update(entity: RentalRecordEntity) = dao.update(entity)
    suspend fun delete(entity: RentalRecordEntity) = dao.delete(entity)
    suspend fun insertAll(entities: List<RentalRecordEntity>) = dao.insertAll(entities)
    suspend fun clear() = dao.clear()
}
