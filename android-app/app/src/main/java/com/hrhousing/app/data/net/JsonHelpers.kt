package com.hrhousing.app.data.net

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

fun JSONObject.optLocalDate(key: String): LocalDate? {
    val value = if (has(key) && !isNull(key)) getString(key) else null
    return value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
}

fun JSONObject.putDate(key: String, date: LocalDate?) {
    put(key, date?.toString() ?: JSONObject.NULL)
}

fun JSONObject.optStringList(key: String): List<String> {
    val array = optJSONArray(key) ?: return listOf("")
    val list = (0 until array.length()).map { array.optString(it, "") }
    return list.ifEmpty { listOf("") }
}

fun JSONObject.putStringList(key: String, list: List<String>) {
    put(key, JSONArray(list))
}
