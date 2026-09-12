package com.hrhousing.app.ui.screens.rentalinfo

import com.hrhousing.app.data.db.entity.ApartmentType
import com.hrhousing.app.data.db.entity.DEPARTMENTS
import com.hrhousing.app.data.db.entity.DepositReturnStatus
import com.hrhousing.app.data.db.entity.PaymentMethod
import com.hrhousing.app.data.db.entity.Periodicity
import com.hrhousing.app.data.db.entity.RentalRecordEntity
import com.hrhousing.app.data.db.entity.TripPurposeType
import com.hrhousing.app.data.db.entity.label
import com.hrhousing.app.util.TimeUtils

/** One editable column of the full "Информация по аренде" table: read + parse-on-blur write-back. */
data class RentalColumn(
    val header: String,
    val widthDp: Int = 140,
    val get: (RentalRecordEntity) -> String,
    val set: (RentalRecordEntity, String) -> RentalRecordEntity,
)

private fun textColumn(header: String, widthDp: Int = 140, get: (RentalRecordEntity) -> String, set: (RentalRecordEntity, String) -> RentalRecordEntity) =
    RentalColumn(header, widthDp, get, set)

private fun longColumn(header: String, widthDp: Int = 120, get: (RentalRecordEntity) -> Long, set: (RentalRecordEntity, Long) -> RentalRecordEntity) =
    RentalColumn(header, widthDp, { get(it).toString() }, { r, v -> set(r, v.filter { c -> c.isDigit() }.toLongOrNull() ?: 0L) })

private fun intColumn(header: String, widthDp: Int = 100, get: (RentalRecordEntity) -> Int, set: (RentalRecordEntity, Int) -> RentalRecordEntity) =
    RentalColumn(header, widthDp, { get(it).toString() }, { r, v -> set(r, v.filter { c -> c.isDigit() }.toIntOrNull() ?: 0) })

private fun dateColumn(header: String, widthDp: Int = 130, get: (RentalRecordEntity) -> java.time.LocalDate?, set: (RentalRecordEntity, java.time.LocalDate?) -> RentalRecordEntity) =
    RentalColumn(header, widthDp, { TimeUtils.format(get(it)) }, { r, v -> set(r, TimeUtils.parseOrNull(v)) })

val RENTAL_INFO_COLUMNS: List<RentalColumn> = listOf(
    textColumn("Код аэропорта", get = { it.airportCode }, set = { r, v -> r.copy(airportCode = v) }),
    textColumn("Город", get = { it.city }, set = { r, v -> r.copy(city = v) }),
    RentalColumn("Периодичность", 120, { if (it.periodicity == Periodicity.DAILY) "Ежедневно" else "Ежемесячно" }, { r, v ->
        r.copy(periodicity = if (v.trim().equals("Ежемесячно", true)) Periodicity.MONTHLY else Periodicity.DAILY)
    }),
    dateColumn("Дата начала аренды", get = { it.rentStartDate }, set = { r, v -> r.copy(rentStartDate = v) }),
    dateColumn("Дата завершения аренды", get = { it.rentEndDate }, set = { r, v -> r.copy(rentEndDate = v) }),
    intColumn("Срок (дней)", get = { it.rentDurationDays }, set = { r, v -> r.copy(rentDurationDays = v) }),
    longColumn("Сумма аренды", get = { it.rentAmount }, set = { r, v -> r.copy(rentAmount = v) }),
    longColumn("Итого сумма аренды", get = { it.rentTotalAmount }, set = { r, v -> r.copy(rentTotalAmount = v) }),
    dateColumn("Дата оплаты аренды", get = { it.rentPaymentDate }, set = { r, v -> r.copy(rentPaymentDate = v) }),
    longColumn("Депозит", get = { it.depositAmount }, set = { r, v -> r.copy(depositAmount = v) }),
    RentalColumn("Возврат депозита", 130, { d ->
        when (d.depositReturnStatus) { DepositReturnStatus.NONE -> ""; DepositReturnStatus.RETURNED -> "Вернули"; DepositReturnStatus.WITH_LANDLORD -> "У арендатора" }
    }, { r, v ->
        r.copy(depositReturnStatus = when (v.trim().lowercase()) { "вернули" -> DepositReturnStatus.RETURNED; "у арендатора" -> DepositReturnStatus.WITH_LANDLORD; else -> DepositReturnStatus.NONE })
    }),
    RentalColumn("Департамент", 180, { it.department }, { r, v -> r.copy(department = DEPARTMENTS.firstOrNull { d -> d.equals(v.trim(), true) } ?: v) }),
    textColumn("ФИО", 160, get = { it.fullName }, set = { r, v -> r.copy(fullName = v) }),
    RentalColumn("Цель поездки", 170, { if (it.purposeType == TripPurposeType.OTHER) "Другое" else "В рамках открытия складов" }, { r, v ->
        r.copy(purposeType = if (v.trim().equals("Другое", true)) TripPurposeType.OTHER else TripPurposeType.WAREHOUSE_OPENING)
    }),
    textColumn("Цель поездки (другое)", 180, get = { it.purposeOtherText }, set = { r, v -> r.copy(purposeOtherText = v) }),
    intColumn("Кол-во комнат", get = { it.roomsCount }, set = { r, v -> r.copy(roomsCount = v) }),
    intColumn("Гостей", 90, get = { it.guestsCount }, set = { r, v -> r.copy(guestsCount = v) }),
    longColumn("Экономия 2-местное", 160, get = { it.savingsTwoPerson }, set = { r, v -> r.copy(savingsTwoPerson = v) }),
    longColumn("Экономия 1-местное", 160, get = { it.savingsOnePerson }, set = { r, v -> r.copy(savingsOnePerson = v) }),
    textColumn("Адрес", 220, get = { it.address }, set = { r, v -> r.copy(address = v) }),
    RentalColumn("Тип квартиры", 130, { it.apartmentType.label() }, { r, v ->
        r.copy(apartmentType = ApartmentType.values().firstOrNull { t -> t.label().equals(v.trim(), true) } ?: r.apartmentType)
    }),
    textColumn("Тип квартиры (другое)", 160, get = { it.apartmentTypeOtherText }, set = { r, v -> r.copy(apartmentTypeOtherText = v) }),
    longColumn("Квартплата", get = { it.utilitiesAmount }, set = { r, v -> r.copy(utilitiesAmount = v) }),
    textColumn("Арендодатель ФИО", 160, get = { it.landlordFullName }, set = { r, v -> r.copy(landlordFullName = v) }),
    textColumn("Арендодатель телефон", 160, get = { it.landlordPhone }, set = { r, v -> r.copy(landlordPhone = v) }),
    RentalColumn("Способ оплаты", 120, { if (it.landlordPaymentMethod == PaymentMethod.INVOICE) "Счёт" else "Перевод" }, { r, v ->
        r.copy(landlordPaymentMethod = if (v.trim().equals("Счёт", true)) PaymentMethod.INVOICE else PaymentMethod.TRANSFER)
    }),
    intColumn("Рейтинг арендодателя", 150, get = { it.landlordRating }, set = { r, v -> r.copy(landlordRating = v.coerceIn(1, 10)) }),
    textColumn("Файл чека", 160, get = { it.receiptFileName }, set = { r, v -> r.copy(receiptFileName = v) }),
    textColumn("Описание чека", 160, get = { it.receiptDescription }, set = { r, v -> r.copy(receiptDescription = v) }),
    textColumn("ЖК", 140, get = { it.complexName }, set = { r, v -> r.copy(complexName = v) }),
    textColumn("Подъезд", 100, get = { it.entrance }, set = { r, v -> r.copy(entrance = v) }),
    textColumn("Примечание к подъезду", 180, get = { it.entranceNote }, set = { r, v -> r.copy(entranceNote = v) }),
    textColumn("Этаж", 90, get = { it.floor }, set = { r, v -> r.copy(floor = v) }),
    textColumn("Номер квартиры", 130, get = { it.apartmentNumber }, set = { r, v -> r.copy(apartmentNumber = v) }),
    textColumn("Код домофона", 130, get = { it.intercomCode }, set = { r, v -> r.copy(intercomCode = v) }),
    textColumn("Время заселения", 130, get = { it.checkinTimeFrom }, set = { r, v -> r.copy(checkinTimeFrom = v) }),
    textColumn("Время выселения", 130, get = { it.checkoutTimeTo }, set = { r, v -> r.copy(checkoutTimeTo = v) }),
    textColumn("2ГИС текст", 130, get = { it.twoGisLinkText }, set = { r, v -> r.copy(twoGisLinkText = v) }),
    textColumn("2ГИС ссылка", 180, get = { it.twoGisLinkUrl }, set = { r, v -> r.copy(twoGisLinkUrl = v) }),
    textColumn("Wi-Fi имя", 130, get = { it.wifiName }, set = { r, v -> r.copy(wifiName = v) }),
    textColumn("Wi-Fi пароль", 130, get = { it.wifiPassword }, set = { r, v -> r.copy(wifiPassword = v) }),
    textColumn("Комментарий", 220, get = { it.additionalComment }, set = { r, v -> r.copy(additionalComment = v) }),
)
