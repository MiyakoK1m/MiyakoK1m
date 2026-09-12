package com.hrhousing.app.data.repo

import com.hrhousing.app.data.db.dao.CityDao
import com.hrhousing.app.data.db.entity.CityEntity
import com.hrhousing.app.data.db.entity.DEFAULT_CITIES
import kotlinx.coroutines.flow.Flow

class CityRepository(private val dao: CityDao) {
    fun observeAll(): Flow<List<CityEntity>> = dao.observeAll()

    suspend fun names(): List<String> = dao.getAll().map { it.name }

    suspend fun ensureSeeded() {
        if (dao.getAll().isEmpty()) resetToDefault()
    }

    suspend fun add(name: String) {
        if (name.isNotBlank()) dao.insert(CityEntity(name = name.trim()))
    }

    suspend fun delete(city: CityEntity) = dao.delete(city)

    suspend fun resetToDefault() {
        dao.clear()
        dao.insertAll(DEFAULT_CITIES.map { CityEntity(name = it) })
    }
}
