package com.hrhousing.app.data.repo

import com.hrhousing.app.data.db.dao.LandlordDao
import com.hrhousing.app.data.db.entity.LandlordEntity
import kotlinx.coroutines.flow.Flow

class LandlordRepository(private val dao: LandlordDao) {
    fun observeAll(): Flow<List<LandlordEntity>> = dao.observeAll()
    fun observeByCity(city: String): Flow<List<LandlordEntity>> = dao.observeByCity(city)
    suspend fun upsert(entity: LandlordEntity) = dao.upsert(entity)
    suspend fun update(entity: LandlordEntity) = dao.update(entity)
    suspend fun delete(entity: LandlordEntity) = dao.delete(entity)
    suspend fun insertAll(entities: List<LandlordEntity>) = dao.insertAll(entities)
    suspend fun clear() = dao.clear()
}
