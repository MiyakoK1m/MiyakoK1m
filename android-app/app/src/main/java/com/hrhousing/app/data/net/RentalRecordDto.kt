package com.hrhousing.app.data.net

import com.hrhousing.app.data.db.entity.ApartmentType
import com.hrhousing.app.data.db.entity.DEPARTMENTS
import com.hrhousing.app.data.db.entity.DepositReturnStatus
import com.hrhousing.app.data.db.entity.PaymentMethod
import com.hrhousing.app.data.db.entity.Periodicity
import com.hrhousing.app.data.db.entity.RentalRecordEntity
import com.hrhousing.app.data.db.entity.TripPurposeType
import org.json.JSONObject

fun RentalRecordEntity.toJson(): JSONObject = JSONObject().apply {
    put("airportCode", airportCode)
    put("city", city)
    put("periodicity", periodicity.name)
    putDate("rentStartDate", rentStartDate)
    putDate("rentEndDate", rentEndDate)
    put("rentDurationDays", rentDurationDays)
    put("rentAmount", rentAmount)
    put("rentTotalAmount", rentTotalAmount)
    putDate("rentPaymentDate", rentPaymentDate)
    put("depositAmount", depositAmount)
    put("depositReturnStatus", depositReturnStatus.name)
    put("department", department)
    put("fullName", fullName)
    put("purposeType", purposeType.name)
    put("purposeOtherText", purposeOtherText)
    put("roomsCount", roomsCount)
    put("guestsCount", guestsCount)
    put("savingsTwoPerson", savingsTwoPerson)
    put("savingsOnePerson", savingsOnePerson)
    put("address", address)
    put("apartmentType", apartmentType.name)
    put("apartmentTypeOtherText", apartmentTypeOtherText)
    put("utilitiesAmount", utilitiesAmount)
    put("landlordFullName", landlordFullName)
    put("landlordPhone", landlordPhone)
    put("landlordPaymentMethod", landlordPaymentMethod.name)
    put("landlordRating", landlordRating)
    put("receiptFileName", receiptFileName)
    put("receiptUri", receiptUri)
    put("receiptDescription", receiptDescription)
    put("complexName", complexName)
    put("entrance", entrance)
    put("entranceNote", entranceNote)
    put("floor", floor)
    put("apartmentNumber", apartmentNumber)
    put("intercomCode", intercomCode)
    put("checkinTimeFrom", checkinTimeFrom)
    put("checkoutTimeTo", checkoutTimeTo)
    put("twoGisLinkText", twoGisLinkText)
    put("twoGisLinkUrl", twoGisLinkUrl)
    put("wifiName", wifiName)
    put("wifiPassword", wifiPassword)
    put("additionalComment", additionalComment)
    put("createdAt", createdAt)
}

fun JSONObject.toRentalRecordEntity(): RentalRecordEntity = RentalRecordEntity(
    id = optLong("id", 0),
    airportCode = optString("airportCode", ""),
    city = optString("city", ""),
    periodicity = runCatching { Periodicity.valueOf(optString("periodicity", "DAILY")) }.getOrDefault(Periodicity.DAILY),
    rentStartDate = optLocalDate("rentStartDate"),
    rentEndDate = optLocalDate("rentEndDate"),
    rentDurationDays = optInt("rentDurationDays", 0),
    rentAmount = optLong("rentAmount", 0),
    rentTotalAmount = optLong("rentTotalAmount", 0),
    rentPaymentDate = optLocalDate("rentPaymentDate"),
    depositAmount = optLong("depositAmount", 0),
    depositReturnStatus = runCatching { DepositReturnStatus.valueOf(optString("depositReturnStatus", "NONE")) }.getOrDefault(DepositReturnStatus.NONE),
    department = DEPARTMENTS.firstOrNull { it == optString("department", "") } ?: optString("department", DEPARTMENTS.first()),
    fullName = optString("fullName", ""),
    purposeType = runCatching { TripPurposeType.valueOf(optString("purposeType", "WAREHOUSE_OPENING")) }.getOrDefault(TripPurposeType.WAREHOUSE_OPENING),
    purposeOtherText = optString("purposeOtherText", ""),
    roomsCount = optInt("roomsCount", 0),
    guestsCount = optInt("guestsCount", 1),
    savingsTwoPerson = optLong("savingsTwoPerson", 0),
    savingsOnePerson = optLong("savingsOnePerson", 0),
    address = optString("address", ""),
    apartmentType = runCatching { ApartmentType.valueOf(optString("apartmentType", "ONE_ROOM")) }.getOrDefault(ApartmentType.ONE_ROOM),
    apartmentTypeOtherText = optString("apartmentTypeOtherText", ""),
    utilitiesAmount = optLong("utilitiesAmount", 0),
    landlordFullName = optString("landlordFullName", ""),
    landlordPhone = optString("landlordPhone", ""),
    landlordPaymentMethod = runCatching { PaymentMethod.valueOf(optString("landlordPaymentMethod", "TRANSFER")) }.getOrDefault(PaymentMethod.TRANSFER),
    landlordRating = optInt("landlordRating", 7),
    receiptFileName = optString("receiptFileName", ""),
    receiptUri = optString("receiptUri", ""),
    receiptDescription = optString("receiptDescription", ""),
    complexName = optString("complexName", ""),
    entrance = optString("entrance", ""),
    entranceNote = optString("entranceNote", ""),
    floor = optString("floor", ""),
    apartmentNumber = optString("apartmentNumber", ""),
    intercomCode = optString("intercomCode", ""),
    checkinTimeFrom = optString("checkinTimeFrom", ""),
    checkoutTimeTo = optString("checkoutTimeTo", ""),
    twoGisLinkText = optString("twoGisLinkText", ""),
    twoGisLinkUrl = optString("twoGisLinkUrl", ""),
    wifiName = optString("wifiName", ""),
    wifiPassword = optString("wifiPassword", ""),
    additionalComment = optString("additionalComment", ""),
    createdAt = optLong("createdAt", System.currentTimeMillis()),
)
