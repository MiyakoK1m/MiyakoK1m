package com.hrhousing.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hrhousing.app.data.db.entity.RentalRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RentalRecordDao {
    @Query("SELECT * FROM rental_records ORDER BY rentStartDate DESC, id DESC")
    fun observeAll(): Flow<List<RentalRecordEntity>>

    @Query("SELECT * FROM rental_records ORDER BY rentStartDate DESC, id DESC")
    suspend fun getAll(): List<RentalRecordEntity>

    @Query("SELECT * FROM rental_records WHERE id = :id")
    suspend fun getById(id: Long): RentalRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RentalRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<RentalRecordEntity>)

    @Update
    suspend fun update(entity: RentalRecordEntity)

    @Delete
    suspend fun delete(entity: RentalRecordEntity)

    @Query("DELETE FROM rental_records")
    suspend fun clear()
}
