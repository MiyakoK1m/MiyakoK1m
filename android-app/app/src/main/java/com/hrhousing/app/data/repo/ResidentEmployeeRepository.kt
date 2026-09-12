package com.hrhousing.app.data.repo

import com.hrhousing.app.data.db.dao.ResidentEmployeeDao
import com.hrhousing.app.data.db.entity.ResidentEmployeeEntity
import kotlinx.coroutines.flow.Flow

class ResidentEmployeeRepository(private val dao: ResidentEmployeeDao) {
    fun observeAll(): Flow<List<ResidentEmployeeEntity>> = dao.observeAll()
    suspend fun getAll(): List<ResidentEmployeeEntity> = dao.getAll()
    suspend fun upsert(entity: ResidentEmployeeEntity) = dao.upsert(entity)
    suspend fun update(entity: ResidentEmployeeEntity) = dao.update(entity)
    suspend fun delete(entity: ResidentEmployeeEntity) = dao.delete(entity)
    suspend fun insertAll(entities: List<ResidentEmployeeEntity>) = dao.insertAll(entities)
    suspend fun clear() = dao.clear()
}
