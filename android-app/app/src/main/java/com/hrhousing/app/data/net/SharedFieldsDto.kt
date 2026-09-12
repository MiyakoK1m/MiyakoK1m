package com.hrhousing.app.data.net

import com.hrhousing.app.data.shared.SharedFields
import org.json.JSONObject

fun SharedFields.toJson(): JSONObject = JSONObject().apply {
    putStringList("guestNames", guestNames)
    put("city", city)
    put("address", address)
    put("complexName", complexName)
    put("apartmentTypeName", apartmentTypeName)
    put("apartmentTypeOtherText", apartmentTypeOtherText)
    put("checkinDateIso", checkinDateIso ?: JSONObject.NULL)
    put("checkoutDateIso", checkoutDateIso ?: JSONObject.NULL)
    put("checkinTime", checkinTime)
    put("checkoutTime", checkoutTime)
    put("dailyRate", dailyRate)
    put("deposit", deposit)
}

fun JSONObject.toSharedFields(): SharedFields = SharedFields(
    guestNames = optStringList("guestNames"),
    city = optString("city", ""),
    address = optString("address", ""),
    complexName = optString("complexName", ""),
    apartmentTypeName = optString("apartmentTypeName", "ONE_ROOM"),
    apartmentTypeOtherText = optString("apartmentTypeOtherText", ""),
    checkinDateIso = if (has("checkinDateIso") && !isNull("checkinDateIso")) getString("checkinDateIso") else null,
    checkoutDateIso = if (has("checkoutDateIso") && !isNull("checkoutDateIso")) getString("checkoutDateIso") else null,
    checkinTime = optString("checkinTime", "14:00"),
    checkoutTime = optString("checkoutTime", "12:00"),
    dailyRate = optLong("dailyRate", 0),
    deposit = optLong("deposit", 0),
)
