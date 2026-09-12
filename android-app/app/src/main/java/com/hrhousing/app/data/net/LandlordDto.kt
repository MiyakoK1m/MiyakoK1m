package com.hrhousing.app.data.net

import com.hrhousing.app.data.db.entity.LandlordEntity
import com.hrhousing.app.data.db.entity.OwnerType
import com.hrhousing.app.data.db.entity.PaymentMethod
import org.json.JSONObject

fun LandlordEntity.toJson(): JSONObject = JSONObject().apply {
    put("fullName", fullName)
    put("phone", phone)
    put("ownerType", ownerType.name)
    put("city", city)
    put("paymentMethod", paymentMethod.name)
    put("rating", rating)
    put("comment", comment)
}

fun JSONObject.toLandlordEntity(): LandlordEntity = LandlordEntity(
    id = optLong("id", 0),
    fullName = optString("fullName", ""),
    phone = optString("phone", ""),
    ownerType = runCatching { OwnerType.valueOf(optString("ownerType", "OWNER")) }.getOrDefault(OwnerType.OWNER),
    city = optString("city", ""),
    paymentMethod = runCatching { PaymentMethod.valueOf(optString("paymentMethod", "TRANSFER")) }.getOrDefault(PaymentMethod.TRANSFER),
    rating = optInt("rating", 7),
    comment = optString("comment", ""),
)
