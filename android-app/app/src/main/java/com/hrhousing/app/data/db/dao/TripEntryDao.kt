package com.hrhousing.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hrhousing.app.data.db.entity.TripEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripEntryDao {
    @Query("SELECT * FROM trip_entries ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TripEntryEntity>>

    @Query("SELECT * FROM trip_entries ORDER BY createdAt DESC")
    suspend fun getAll(): List<TripEntryEntity>

    @Query("SELECT * FROM trip_entries WHERE city = :city")
    suspend fun getByCity(city: String): List<TripEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TripEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<TripEntryEntity>)

    @Update
    suspend fun update(entity: TripEntryEntity)

    @Delete
    suspend fun delete(entity: TripEntryEntity)

    @Query("DELETE FROM trip_entries")
    suspend fun clear()
}
