package com.hrhousing.app.data.net

import com.hrhousing.app.data.db.entity.CityEntity
import org.json.JSONObject

fun CityEntity.toJson(): JSONObject = JSONObject().apply {
    put("name", name)
}

fun JSONObject.toCityEntity(): CityEntity = CityEntity(
    id = optLong("id", 0),
    name = optString("name", ""),
)
