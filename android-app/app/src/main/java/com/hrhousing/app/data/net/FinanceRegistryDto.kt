package com.hrhousing.app.data.net

import com.hrhousing.app.data.db.entity.ApartmentType
import com.hrhousing.app.data.db.entity.FinanceRegistryEntryEntity
import org.json.JSONObject

fun FinanceRegistryEntryEntity.toJson(): JSONObject = JSONObject().apply {
    put("savedAt", savedAt)
    put("city", city)
    put("fullName", fullName)
    put("apartmentType", apartmentType.name)
    put("apartmentTypeOtherText", apartmentTypeOtherText)
    put("guestsCount", guestsCount)
    putDate("checkinDate", checkinDate)
    putDate("checkoutDate", checkoutDate)
    put("nights", nights)
    put("dailyRate", dailyRate)
    put("deposit", deposit)
    put("total", total)
    put("comment", comment)
}

fun JSONObject.toFinanceRegistryEntity(): FinanceRegistryEntryEntity = FinanceRegistryEntryEntity(
    id = optLong("id", 0),
    savedAt = optLong("savedAt", System.currentTimeMillis()),
    city = optString("city", ""),
    fullName = optString("fullName", ""),
    apartmentType = runCatching { ApartmentType.valueOf(optString("apartmentType", "ONE_ROOM")) }.getOrDefault(ApartmentType.ONE_ROOM),
    apartmentTypeOtherText = optString("apartmentTypeOtherText", ""),
    guestsCount = optInt("guestsCount", 1),
    checkinDate = optLocalDate("checkinDate"),
    checkoutDate = optLocalDate("checkoutDate"),
    nights = optInt("nights", 0),
    dailyRate = optLong("dailyRate", 0),
    deposit = optLong("deposit", 0),
    total = optLong("total", 0),
    comment = optString("comment", ""),
)
