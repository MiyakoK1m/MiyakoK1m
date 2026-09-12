package com.hrhousing.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hrhousing.app.data.db.entity.FinanceRegistryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceRegistryDao {
    @Query("SELECT * FROM finance_registry ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<FinanceRegistryEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FinanceRegistryEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<FinanceRegistryEntryEntity>)

    @Delete
    suspend fun delete(entity: FinanceRegistryEntryEntity)

    @Query("DELETE FROM finance_registry")
    suspend fun clear()
}
