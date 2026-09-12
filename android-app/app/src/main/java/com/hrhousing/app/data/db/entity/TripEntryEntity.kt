package com.hrhousing.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class HousingType { APARTMENT, HOTEL }
enum class TripPurposeType { WAREHOUSE_OPENING, OTHER }

val DEPARTMENTS = listOf(
    "IT", "БиОТ", "Инфраструктура", "Коммерческий департамент", "Логистика",
    "НО", "Отдел кадров", "Проектный и процессный отдел", "СБ", "СП", "Склад",
)

@Entity(tableName = "trip_entries")
data class TripEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val guestNames: List<String> = listOf(""),
    val position: String = "",
    val department: String = DEPARTMENTS.first(),
    val housingType: HousingType = HousingType.APARTMENT,
    val offRegulation: Boolean = false,
    val city: String = "",
    val checkinDate: LocalDate? = null,
    val checkoutDate: LocalDate? = null,
    val perDiem: Long = 0,
    val urgencyReason: String = "",
    val justification: String = "",
    val purposeType: TripPurposeType = TripPurposeType.WAREHOUSE_OPENING,
    val purposeOtherText: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
