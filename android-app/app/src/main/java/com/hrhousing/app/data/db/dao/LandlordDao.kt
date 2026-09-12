package com.hrhousing.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hrhousing.app.data.db.entity.LandlordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LandlordDao {
    @Query("SELECT * FROM landlords ORDER BY rating DESC, fullName ASC")
    fun observeAll(): Flow<List<LandlordEntity>>

    @Query("SELECT * FROM landlords WHERE city = :city ORDER BY rating DESC")
    fun observeByCity(city: String): Flow<List<LandlordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LandlordEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<LandlordEntity>)

    @Update
    suspend fun update(entity: LandlordEntity)

    @Delete
    suspend fun delete(entity: LandlordEntity)

    @Query("DELETE FROM landlords")
    suspend fun clear()
}
