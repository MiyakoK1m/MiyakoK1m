package com.hrhousing.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hrhousing.app.data.db.dao.CityDao
import com.hrhousing.app.data.db.dao.FinanceRegistryDao
import com.hrhousing.app.data.db.dao.LandlordDao
import com.hrhousing.app.data.db.dao.RentalRecordDao
import com.hrhousing.app.data.db.dao.ResidentEmployeeDao
import com.hrhousing.app.data.db.dao.TripEntryDao
import com.hrhousing.app.data.db.entity.CityEntity
import com.hrhousing.app.data.db.entity.FinanceRegistryEntryEntity
import com.hrhousing.app.data.db.entity.LandlordEntity
import com.hrhousing.app.data.db.entity.RentalRecordEntity
import com.hrhousing.app.data.db.entity.ResidentEmployeeEntity
import com.hrhousing.app.data.db.entity.TripEntryEntity

@Database(
    entities = [
        CityEntity::class,
        ResidentEmployeeEntity::class,
        LandlordEntity::class,
        TripEntryEntity::class,
        RentalRecordEntity::class,
        FinanceRegistryEntryEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao
    abstract fun residentEmployeeDao(): ResidentEmployeeDao
    abstract fun landlordDao(): LandlordDao
    abstract fun tripEntryDao(): TripEntryDao
    abstract fun rentalRecordDao(): RentalRecordDao
    abstract fun financeRegistryDao(): FinanceRegistryDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hr_housing.db",
                ).build().also { instance = it }
            }
    }
}
