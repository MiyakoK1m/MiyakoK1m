package com.hrhousing.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class Periodicity { DAILY, MONTHLY }
enum class DepositReturnStatus { NONE, RETURNED, WITH_LANDLORD }
enum class ApartmentType { STUDIO, ONE_ROOM, TWO_ROOM, THREE_ROOM, FOUR_ROOM, OTHER }

fun ApartmentType.label(): String = when (this) {
    ApartmentType.STUDIO -> "Студия"
    ApartmentType.ONE_ROOM -> "1-комнатная"
    ApartmentType.TWO_ROOM -> "2-комнатная"
    ApartmentType.THREE_ROOM -> "3-комнатная"
    ApartmentType.FOUR_ROOM -> "4-комнатная"
    ApartmentType.OTHER -> "Другое"
}

fun ApartmentType.roomsCount(): Int = when (this) {
    ApartmentType.STUDIO -> 0
    ApartmentType.ONE_ROOM -> 1
    ApartmentType.TWO_ROOM -> 2
    ApartmentType.THREE_ROOM -> 3
    ApartmentType.FOUR_ROOM -> 4
    ApartmentType.OTHER -> 0
}

/**
 * The single "rental" record. It doubles as the row shown in "База данных" / "Информация по
 * аренде" (section 7/8) AND carries the extended check-in fields from "Информация по заселению"
 * (section 4), since section 8 explicitly displays those extended columns for the same record
 * and several fields are cross-synced between the two screens.
 */
@Entity(tableName = "rental_records")
data class RentalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    // --- Раздел 7: База данных ---
    val airportCode: String = "",
    val city: String = "",
    val periodicity: Periodicity = Periodicity.DAILY,
    val rentStartDate: LocalDate? = null,
    val rentEndDate: LocalDate? = null,
    val rentDurationDays: Int = 0,
    val rentAmount: Long = 0,
    val rentTotalAmount: Long = 0,
    val rentPaymentDate: LocalDate? = null,
    val depositAmount: Long = 0,
    val depositReturnStatus: DepositReturnStatus = DepositReturnStatus.NONE,
    val department: String = DEPARTMENTS.first(),
    val fullName: String = "",
    val purposeType: TripPurposeType = TripPurposeType.WAREHOUSE_OPENING,
    val purposeOtherText: String = "",
    val roomsCount: Int = 0,
    val guestsCount: Int = 1,
    val savingsTwoPerson: Long = 0,
    val savingsOnePerson: Long = 0,
    val address: String = "",
    val apartmentType: ApartmentType = ApartmentType.ONE_ROOM,
    val apartmentTypeOtherText: String = "",
    val utilitiesAmount: Long = 0,

    // Контакты арендодателя
    val landlordFullName: String = "",
    val landlordPhone: String = "",
    val landlordPaymentMethod: PaymentMethod = PaymentMethod.TRANSFER,
    val landlordRating: Int = 7,

    // Чек
    val receiptFileName: String = "",
    val receiptUri: String = "",
    val receiptDescription: String = "",

    // --- Раздел 4: Информация по заселению (расширенные поля) ---
    val complexName: String = "",
    val entrance: String = "",
    val entranceNote: String = "",
    val floor: String = "",
    val apartmentNumber: String = "",
    val intercomCode: String = "",
    val checkinTimeFrom: String = "",
    val checkoutTimeTo: String = "",
    val twoGisLinkText: String = "",
    val twoGisLinkUrl: String = "",
    val wifiName: String = "",
    val wifiPassword: String = "",
    val includeRulesBlock: Boolean = true,
    val additionalComment: String = "",

    val createdAt: Long = System.currentTimeMillis(),
)
