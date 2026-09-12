package com.hrhousing.app.data.repo

import com.hrhousing.app.data.db.dao.TripEntryDao
import com.hrhousing.app.data.db.entity.TripEntryEntity
import kotlinx.coroutines.flow.Flow

class TripEntryRepository(private val dao: TripEntryDao) {
    fun observeAll(): Flow<List<TripEntryEntity>> = dao.observeAll()
    suspend fun getAll(): List<TripEntryEntity> = dao.getAll()
    suspend fun getByCity(city: String): List<TripEntryEntity> = dao.getByCity(city)
    suspend fun upsert(entity: TripEntryEntity): Long = dao.upsert(entity)
    suspend fun update(entity: TripEntryEntity) = dao.update(entity)
    suspend fun delete(entity: TripEntryEntity) = dao.delete(entity)
    suspend fun insertAll(entities: List<TripEntryEntity>) = dao.insertAll(entities)
    suspend fun clear() = dao.clear()
}
