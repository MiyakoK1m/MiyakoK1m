package com.hrhousing.app.util.excel

import com.hrhousing.app.data.db.entity.ApartmentType
import com.hrhousing.app.data.db.entity.DEPARTMENTS
import com.hrhousing.app.data.db.entity.DepositReturnStatus
import com.hrhousing.app.data.db.entity.PaymentMethod
import com.hrhousing.app.data.db.entity.Periodicity
import com.hrhousing.app.data.db.entity.RentalRecordEntity
import com.hrhousing.app.data.db.entity.TripPurposeType
import com.hrhousing.app.data.db.entity.label
import com.hrhousing.app.data.db.entity.roomsCount
import com.hrhousing.app.util.TimeUtils
import com.hrhousing.app.util.formatMoney
import java.time.LocalDate

const val SHEET_RENTAL_RECORDS = "База данных аренды"

val RENTAL_RECORD_HEADERS = listOf(
    "Код аэропорта", "Город", "Периодичность", "Дата начала аренды", "Дата завершения аренды",
    "Срок аренды (дней)", "Сумма аренды", "Итого сумма аренды", "Дата оплаты аренды",
    "Сумма депозита", "Статус возврата депозита", "Департамент", "ФИО", "Цель поездки",
    "Цель поездки (другое)", "Кол-во комнат", "Гостей", "Экономия 2-местное", "Экономия 1-местное",
    "Адрес", "Тип квартиры", "Тип квартиры (другое)", "Квартплата",
    "Арендодатель ФИО", "Арендодатель телефон", "Способ оплаты", "Рейтинг арендодателя",
    "Файл чека", "Описание чека",
    "ЖК", "Подъезд", "Примечание к подъезду", "Этаж", "Номер квартиры", "Код домофона",
    "Время заселения", "Время выселения", "2ГИС текст", "2ГИС ссылка", "Wi-Fi имя", "Wi-Fi пароль",
    "Комментарий",
)

fun RentalRecordEntity.toExcelRow(): List<Cv> = listOf(
    Cv.of(airportCode), Cv.of(city), Cv.of(if (periodicity == Periodicity.DAILY) "Ежедневно" else "Ежемесячно"),
    Cv.of(TimeUtils.format(rentStartDate)), Cv.of(TimeUtils.format(rentEndDate)),
    Cv.of(rentDurationDays), Cv.of(rentAmount), Cv.of(rentTotalAmount), Cv.of(TimeUtils.format(rentPaymentDate)),
    Cv.of(depositAmount), Cv.of(depositReturnStatusLabel(depositReturnStatus)), Cv.of(department), Cv.of(fullName),
    Cv.of(purposeLabel(purposeType)), Cv.of(purposeOtherText), Cv.of(roomsCount), Cv.of(guestsCount),
    Cv.of(savingsTwoPerson), Cv.of(savingsOnePerson), Cv.of(address), Cv.of(apartmentType.label()),
    Cv.of(apartmentTypeOtherText), Cv.of(utilitiesAmount),
    Cv.of(landlordFullName), Cv.of(landlordPhone), Cv.of(if (landlordPaymentMethod == PaymentMethod.INVOICE) "Счёт" else "Перевод"),
    Cv.of(landlordRating), Cv.of(receiptFileName), Cv.of(receiptDescription),
    Cv.of(complexName), Cv.of(entrance), Cv.of(entranceNote), Cv.of(floor), Cv.of(apartmentNumber), Cv.of(intercomCode),
    Cv.of(checkinTimeFrom), Cv.of(checkoutTimeTo), Cv.of(twoGisLinkText), Cv.of(twoGisLinkUrl), Cv.of(wifiName), Cv.of(wifiPassword),
    Cv.of(additionalComment),
)

fun ImportRow.toRentalRecordEntity(): RentalRecordEntity {
    val periodicity = if (get("Периодичность").trim().equals("Ежемесячно", true)) Periodicity.MONTHLY else Periodicity.DAILY
    val apartmentType = ApartmentType.values().firstOrNull { it.label().equals(get("Тип квартиры").trim(), true) } ?: ApartmentType.ONE_ROOM
    val depositStatus = when (get("Статус возврата депозита").trim().lowercase()) {
        "вернули" -> DepositReturnStatus.RETURNED
        "у арендатора" -> DepositReturnStatus.WITH_LANDLORD
        else -> DepositReturnStatus.NONE
    }
    val paymentMethod = if (get("Способ оплаты").trim().equals("Счёт", true)) PaymentMethod.INVOICE else PaymentMethod.TRANSFER
    val purposeType = if (get("Цель поездки").trim().equals("Другое", true)) TripPurposeType.OTHER else TripPurposeType.WAREHOUSE_OPENING
    val department = DEPARTMENTS.firstOrNull { it.equals(get("Департамент").trim(), true) } ?: get("Департамент").ifBlank { DEPARTMENTS.first() }

    return RentalRecordEntity(
        airportCode = get("Код аэропорта"),
        city = get("Город"),
        periodicity = periodicity,
        rentStartDate = TimeUtils.parseOrNull(get("Дата начала аренды")),
        rentEndDate = TimeUtils.parseOrNull(get("Дата завершения аренды")),
        rentDurationDays = get("Срок аренды (дней)").toIntOrNull() ?: 0,
        rentAmount = get("Сумма аренды").toLongOrNull() ?: 0,
        rentTotalAmount = get("Итого сумма аренды").toLongOrNull() ?: 0,
        rentPaymentDate = TimeUtils.parseOrNull(get("Дата оплаты аренды")),
        depositAmount = get("Сумма депозита").toLongOrNull() ?: 0,
        depositReturnStatus = depositStatus,
        department = department,
        fullName = get("ФИО"),
        purposeType = purposeType,
        purposeOtherText = get("Цель поездки (другое)"),
        roomsCount = get("Кол-во комнат").toIntOrNull() ?: apartmentType.roomsCount(),
        guestsCount = get("Гостей").toIntOrNull() ?: 1,
        savingsTwoPerson = get("Экономия 2-местное").toLongOrNull() ?: 0,
        savingsOnePerson = get("Экономия 1-местное").toLongOrNull() ?: 0,
        address = get("Адрес"),
        apartmentType = apartmentType,
        apartmentTypeOtherText = get("Тип квартиры (другое)"),
        utilitiesAmount = get("Квартплата").toLongOrNull() ?: 0,
        landlordFullName = get("Арендодатель ФИО"),
        landlordPhone = get("Арендодатель телефон"),
        landlordPaymentMethod = paymentMethod,
        landlordRating = get("Рейтинг арендодателя").toIntOrNull() ?: 7,
        receiptFileName = get("Файл чека"),
        receiptDescription = get("Описание чека"),
        complexName = get("ЖК"),
        entrance = get("Подъезд"),
        entranceNote = get("Примечание к подъезду"),
        floor = get("Этаж"),
        apartmentNumber = get("Номер квартиры"),
        intercomCode = get("Код домофона"),
        checkinTimeFrom = get("Время заселения"),
        checkoutTimeTo = get("Время выселения"),
        twoGisLinkText = get("2ГИС текст"),
        twoGisLinkUrl = get("2ГИС ссылка"),
        wifiName = get("Wi-Fi имя"),
        wifiPassword = get("Wi-Fi пароль"),
        additionalComment = get("Комментарий"),
    )
}

private fun depositReturnStatusLabel(status: DepositReturnStatus) = when (status) {
    DepositReturnStatus.NONE -> ""
    DepositReturnStatus.RETURNED -> "Вернули"
    DepositReturnStatus.WITH_LANDLORD -> "У арендатора"
}

private fun purposeLabel(type: TripPurposeType) = when (type) {
    TripPurposeType.WAREHOUSE_OPENING -> "В рамках открытия складов"
    TripPurposeType.OTHER -> "Другое"
}

