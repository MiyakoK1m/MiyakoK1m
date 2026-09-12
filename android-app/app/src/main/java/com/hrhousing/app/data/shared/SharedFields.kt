package com.hrhousing.app.data.shared

import com.hrhousing.app.data.db.entity.ApartmentType
import kotlinx.serialization.Serializable

/**
 * The fields that must stay in sync, live, across "Входная информация", "Бронирование",
 * "Согласование с финансами" и "Информация по заселению" (see «Сквозная синхронизация полей»
 * in the spec). Dates/times are stored as plain ISO/text so this stays trivially serializable.
 */
@Serializable
data class SharedFields(
    val guestNames: List<String> = listOf(""),
    val city: String = "",
    val address: String = "",
    val complexName: String = "",
    val apartmentTypeName: String = ApartmentType.ONE_ROOM.name,
    val apartmentTypeOtherText: String = "",
    val checkinDateIso: String? = null,
    val checkoutDateIso: String? = null,
    val checkinTime: String = "14:00",
    val checkoutTime: String = "12:00",
    val dailyRate: Long = 0,
    val deposit: Long = 0,
) {
    val apartmentType: ApartmentType
        get() = try { ApartmentType.valueOf(apartmentTypeName) } catch (e: IllegalArgumentException) { ApartmentType.ONE_ROOM }
}
