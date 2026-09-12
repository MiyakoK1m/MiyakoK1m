package com.hrhousing.app.util.excel

import com.hrhousing.app.data.db.entity.ApartmentType
import com.hrhousing.app.data.db.entity.CityEntity
import com.hrhousing.app.data.db.entity.FinanceRegistryEntryEntity
import com.hrhousing.app.data.db.entity.LandlordEntity
import com.hrhousing.app.data.db.entity.OwnerType
import com.hrhousing.app.data.db.entity.PaymentMethod
import com.hrhousing.app.data.db.entity.ResidentEmployeeEntity
import com.hrhousing.app.data.db.entity.label
import com.hrhousing.app.util.TimeUtils

// ---------- Арендодатели ----------
const val SHEET_LANDLORDS = "Арендодатели"
val LANDLORD_HEADERS = listOf("ФИО", "Контактный номер", "Собственник/риелтор", "Город", "Способ оплаты", "Рейтинг", "Комментарий")

fun LandlordEntity.toExcelRow(): List<Cv> = listOf(
    Cv.of(fullName), Cv.of(phone),
    Cv.of(if (ownerType == OwnerType.OWNER) "Собственник" else "Риелтор"),
    Cv.of(city), Cv.of(if (paymentMethod == PaymentMethod.INVOICE) "Счёт" else "Перевод"),
    Cv.of(rating), Cv.of(comment),
)

fun ImportRow.toLandlordEntity(): LandlordEntity = LandlordEntity(
    fullName = get("ФИО"),
    phone = get("Контактный номер"),
    ownerType = if (get("Собственник/риелтор").trim().equals("Риелтор", true)) OwnerType.AGENT else OwnerType.OWNER,
    city = get("Город"),
    paymentMethod = if (get("Способ оплаты").trim().equals("Счёт", true)) PaymentMethod.INVOICE else PaymentMethod.TRANSFER,
    rating = get("Рейтинг").toIntOrNull() ?: 7,
    comment = get("Комментарий"),
)

// ---------- Проживающие сотрудники ----------
const val SHEET_RESIDENTS = "Проживающие сотрудники"
val RESIDENT_HEADERS = listOf("ФИО сотрудника", "Комментарий")

fun ResidentEmployeeEntity.toExcelRow(): List<Cv> = listOf(Cv.of(fullName), Cv.of(comment))

fun ImportRow.toResidentEntity(): ResidentEmployeeEntity =
    ResidentEmployeeEntity(fullName = get("ФИО сотрудника", "ФИО"), comment = get("Комментарий"))

// ---------- Города ----------
const val SHEET_CITIES = "Города"
val CITY_HEADERS = listOf("Город")

fun CityEntity.toExcelRow(): List<Cv> = listOf(Cv.of(name))

fun ImportRow.toCityEntity(): CityEntity = CityEntity(name = get("Город"))

// ---------- Реестр согласований с финансами ----------
const val SHEET_FINANCE_REGISTRY = "Реестр согласований"
val FINANCE_REGISTRY_HEADERS = listOf(
    "Дата сохранения", "Город", "ФИО", "Тип квартиры", "Гостей", "Заезд", "Выезд",
    "Суток", "Ставка за сутки", "Депозит", "Итого", "Комментарий",
)

fun FinanceRegistryEntryEntity.toExcelRow(): List<Cv> = listOf(
    Cv.of(TimeUtils.format(java.time.Instant.ofEpochMilli(savedAt).atZone(TimeUtils.ALMATY_ZONE).toLocalDate())),
    Cv.of(city), Cv.of(fullName),
    Cv.of(if (apartmentType == ApartmentType.OTHER) apartmentTypeOtherText else apartmentType.label()),
    Cv.of(guestsCount), Cv.of(TimeUtils.format(checkinDate)), Cv.of(TimeUtils.format(checkoutDate)),
    Cv.of(nights), Cv.of(dailyRate), Cv.of(deposit), Cv.of(total), Cv.of(comment),
)
