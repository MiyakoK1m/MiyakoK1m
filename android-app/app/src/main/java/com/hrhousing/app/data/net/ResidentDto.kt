package com.hrhousing.app.data.net

import com.hrhousing.app.data.db.entity.ResidentEmployeeEntity
import org.json.JSONObject

fun ResidentEmployeeEntity.toJson(): JSONObject = JSONObject().apply {
    put("fullName", fullName)
    put("comment", comment)
}

fun JSONObject.toResidentEntity(): ResidentEmployeeEntity = ResidentEmployeeEntity(
    id = optLong("id", 0),
    fullName = optString("fullName", ""),
    comment = optString("comment", ""),
)
