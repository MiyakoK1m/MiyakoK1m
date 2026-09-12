package com.hrhousing.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hrhousing.app.data.db.entity.ResidentEmployeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResidentEmployeeDao {
    @Query("SELECT * FROM resident_employees ORDER BY fullName ASC")
    fun observeAll(): Flow<List<ResidentEmployeeEntity>>

    @Query("SELECT * FROM resident_employees ORDER BY fullName ASC")
    suspend fun getAll(): List<ResidentEmployeeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ResidentEmployeeEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<ResidentEmployeeEntity>)

    @Update
    suspend fun update(entity: ResidentEmployeeEntity)

    @Delete
    suspend fun delete(entity: ResidentEmployeeEntity)

    @Query("DELETE FROM resident_employees")
    suspend fun clear()
}
