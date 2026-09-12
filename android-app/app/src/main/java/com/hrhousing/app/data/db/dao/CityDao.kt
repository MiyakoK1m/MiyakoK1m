package com.hrhousing.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hrhousing.app.data.db.entity.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Query("SELECT * FROM cities ORDER BY name ASC")
    fun observeAll(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities ORDER BY name ASC")
    suspend fun getAll(): List<CityEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(city: CityEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cities: List<CityEntity>)

    @Delete
    suspend fun delete(city: CityEntity)

    @Query("DELETE FROM cities")
    suspend fun clear()
}
