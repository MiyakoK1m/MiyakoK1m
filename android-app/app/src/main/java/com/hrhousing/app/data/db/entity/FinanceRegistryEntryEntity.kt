package com.hrhousing.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/** A saved row in the independent "Реестр согласований с финансами" (section 6). */
@Entity(tableName = "finance_registry")
data class FinanceRegistryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val savedAt: Long = System.currentTimeMillis(),
    val city: String = "",
    val fullName: String = "",
    val apartmentType: ApartmentType = ApartmentType.ONE_ROOM,
    val apartmentTypeOtherText: String = "",
    val guestsCount: Int = 1,
    val checkinDate: LocalDate? = null,
    val checkoutDate: LocalDate? = null,
    val nights: Int = 0,
    val dailyRate: Long = 0,
    val deposit: Long = 0,
    val total: Long = 0,
    val comment: String = "",
)
