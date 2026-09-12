package com.hrhousing.app.data.net

import com.hrhousing.app.data.db.entity.DEPARTMENTS
import com.hrhousing.app.data.db.entity.HousingType
import com.hrhousing.app.data.db.entity.TripEntryEntity
import com.hrhousing.app.data.db.entity.TripPurposeType
import org.json.JSONObject

fun TripEntryEntity.toJson(): JSONObject = JSONObject().apply {
    putStringList("guestNames", guestNames)
    put("position", position)
    put("department", department)
    put("housingType", housingType.name)
    put("offRegulation", offRegulation)
    put("city", city)
    putDate("checkinDate", checkinDate)
    putDate("checkoutDate", checkoutDate)
    put("perDiem", perDiem)
    put("urgencyReason", urgencyReason)
    put("justification", justification)
    put("purposeType", purposeType.name)
    put("purposeOtherText", purposeOtherText)
    put("createdAt", createdAt)
}

fun JSONObject.toTripEntryEntity(): TripEntryEntity = TripEntryEntity(
    id = optLong("id", 0),
    guestNames = optStringList("guestNames"),
    position = optString("position", ""),
    department = DEPARTMENTS.firstOrNull { it == optString("department", "") } ?: optString("department", DEPARTMENTS.first()),
    housingType = runCatching { HousingType.valueOf(optString("housingType", "APARTMENT")) }.getOrDefault(HousingType.APARTMENT),
    offRegulation = optBoolean("offRegulation", false),
    city = optString("city", ""),
    checkinDate = optLocalDate("checkinDate"),
    checkoutDate = optLocalDate("checkoutDate"),
    perDiem = optLong("perDiem", 0),
    urgencyReason = optString("urgencyReason", ""),
    justification = optString("justification", ""),
    purposeType = runCatching { TripPurposeType.valueOf(optString("purposeType", "WAREHOUSE_OPENING")) }.getOrDefault(TripPurposeType.WAREHOUSE_OPENING),
    purposeOtherText = optString("purposeOtherText", ""),
    createdAt = optLong("createdAt", System.currentTimeMillis()),
)
