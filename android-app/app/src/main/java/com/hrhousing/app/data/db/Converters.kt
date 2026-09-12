package com.hrhousing.app.data.db

import androidx.room.TypeConverter
import com.hrhousing.app.data.db.entity.ApartmentType
import com.hrhousing.app.data.db.entity.DepositReturnStatus
import com.hrhousing.app.data.db.entity.HousingType
import com.hrhousing.app.data.db.entity.OwnerType
import com.hrhousing.app.data.db.entity.PaymentMethod
import com.hrhousing.app.data.db.entity.Periodicity
import com.hrhousing.app.data.db.entity.TripPurposeType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

/**
 * Room needs primitive column types; LocalDate is stored as an ISO-8601 string so it stays
 * human-readable when the database file is inspected directly, and lists are stored as JSON.
 */
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? = list?.let { json.encodeToString(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? = value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromHousingType(value: HousingType): String = value.name
    @TypeConverter
    fun toHousingType(value: String): HousingType = HousingType.valueOf(value)

    @TypeConverter
    fun fromTripPurposeType(value: TripPurposeType): String = value.name
    @TypeConverter
    fun toTripPurposeType(value: String): TripPurposeType = TripPurposeType.valueOf(value)

    @TypeConverter
    fun fromPeriodicity(value: Periodicity): String = value.name
    @TypeConverter
    fun toPeriodicity(value: String): Periodicity = Periodicity.valueOf(value)

    @TypeConverter
    fun fromDepositReturnStatus(value: DepositReturnStatus): String = value.name
    @TypeConverter
    fun toDepositReturnStatus(value: String): DepositReturnStatus = DepositReturnStatus.valueOf(value)

    @TypeConverter
    fun fromApartmentType(value: ApartmentType): String = value.name
    @TypeConverter
    fun toApartmentType(value: String): ApartmentType = ApartmentType.valueOf(value)

    @TypeConverter
    fun fromOwnerType(value: OwnerType): String = value.name
    @TypeConverter
    fun toOwnerType(value: String): OwnerType = OwnerType.valueOf(value)

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name
    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)
}
