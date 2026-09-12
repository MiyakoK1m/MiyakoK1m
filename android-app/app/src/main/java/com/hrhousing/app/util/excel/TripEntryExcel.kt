package com.hrhousing.app.util.excel

import com.hrhousing.app.data.db.entity.DEPARTMENTS
import com.hrhousing.app.data.db.entity.HousingType
import com.hrhousing.app.data.db.entity.TripEntryEntity
import com.hrhousing.app.data.db.entity.TripPurposeType
import com.hrhousing.app.util.TimeUtils

const val SHEET_TRIP_ENTRIES = "Входная информация"
val TRIP_ENTRY_HEADERS = listOf(
    "ФИО проживающих", "Должность", "Департамент", "Вид жилья", "Вне регламента", "Город назначения",
    "Дата заселения", "Дата выселения", "Сумма суточных", "Причина срочности", "Обоснование командировки",
    "Цель поездки", "Цель поездки (другое)",
)

fun TripEntryEntity.toExcelRow(): List<Cv> = listOf(
    Cv.of(guestNames.filter { it.isNotBlank() }.joinToString("; ")),
    Cv.of(position), Cv.of(department), Cv.of(if (housingType == HousingType.APARTMENT) "Квартира" else "Отель"),
    Cv.of(if (offRegulation) "Да" else "Нет"), Cv.of(city),
    Cv.of(TimeUtils.format(checkinDate)), Cv.of(TimeUtils.format(checkoutDate)), Cv.of(perDiem),
    Cv.of(urgencyReason), Cv.of(justification),
    Cv.of(if (purposeType == TripPurposeType.OTHER) "Другое" else "В рамках открытия складов"),
    Cv.of(purposeOtherText),
)

/**
 * Generic importer for files exported by this app itself (round-trips [toExcelRow]) or any
 * spreadsheet with similarly-named headers, using the same exact-then-partial header matching
 * as the rental import.
 */
fun ImportRow.toTripEntryEntity(): TripEntryEntity {
    val guests = get("ФИО проживающих", "ФИО").split(";", "\n")
        .map { it.trim() }.filter { it.isNotEmpty() }
        .ifEmpty { listOf("") }
    val department = DEPARTMENTS.firstOrNull { it.equals(get("Департамент").trim(), true) } ?: get("Департамент").ifBlank { DEPARTMENTS.first() }
    return TripEntryEntity(
        guestNames = guests,
        position = get("Должность"),
        department = department,
        housingType = if (get("Вид жилья").trim().equals("Отель", true)) HousingType.HOTEL else HousingType.APARTMENT,
        offRegulation = get("Вне регламента").trim().equals("Да", true),
        city = get("Город назначения", "Город"),
        checkinDate = TimeUtils.parseOrNull(get("Дата заселения")),
        checkoutDate = TimeUtils.parseOrNull(get("Дата выселения")),
        perDiem = get("Сумма суточных").toLongOrNull() ?: 0,
        urgencyReason = get("Причина срочности"),
        justification = get("Обоснование командировки"),
        purposeType = if (get("Цель поездки").trim().equals("Другое", true)) TripPurposeType.OTHER else TripPurposeType.WAREHOUSE_OPENING,
        purposeOtherText = get("Цель поездки (другое)"),
    )
}

/**
 * Import for the company's own trip-tracking system export.
 *
 * The real corporate export format/column names are not available yet — this deliberately reuses
 * the generic header matching above (so any export with recognisably-named columns already works),
 * but is the single place to adjust once a real sample file exists: add the corporate system's
 * exact column headers as extra candidates to each `get(...)` call in [toTripEntryEntity], or add a
 * bespoke `toTripEntryEntityFromCorporateFormat()` here that reads the corporate sheet's specific
 * layout (e.g. a different header row offset, merged cells, or a different sheet name).
 */
fun ImportRow.toTripEntryEntityFromCorporateFormat(): TripEntryEntity = toTripEntryEntity()
