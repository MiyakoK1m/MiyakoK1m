package com.hrhousing.app.data.shared

import com.hrhousing.app.data.db.entity.ApartmentType
import com.hrhousing.app.data.prefs.JsonPrefStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

private const val PREF_KEY = "shared_fields_json"

/**
 * App-wide bus so that editing e.g. "Город" on one screen updates it everywhere it's shown
 * (see «Сквозная синхронизация полей»), without requiring the user to save any form. Lives as
 * long as the process (held in AppContainer), and is also persisted so a value typed on one
 * screen survives navigating away and the app being killed in the background.
 */
class SharedFieldsRepository(private val jsonPrefStore: JsonPrefStore, private val scope: CoroutineScope) {
    private val json = Json { ignoreUnknownKeys = true }
    private val _fields = MutableStateFlow(SharedFields())
    val fields: StateFlow<SharedFields> = _fields

    init {
        scope.launch {
            jsonPrefStore.observeRaw(PREF_KEY).first()?.let { raw ->
                runCatching { json.decodeFromString<SharedFields>(raw) }.getOrNull()?.let { _fields.value = it }
            }
        }
    }

    private fun persist(value: SharedFields) {
        scope.launch { jsonPrefStore.writeRaw(PREF_KEY, json.encodeToString(value)) }
    }

    fun update(transform: (SharedFields) -> SharedFields) {
        val next = transform(_fields.value)
        _fields.value = next
        persist(next)
    }

    fun setGuestNames(names: List<String>) = update { it.copy(guestNames = names) }
    fun setCity(city: String) = update { it.copy(city = city) }
    fun setAddress(address: String) = update { it.copy(address = address) }
    fun setComplexName(name: String) = update { it.copy(complexName = name) }
    fun setApartmentType(type: ApartmentType, otherText: String = _fields.value.apartmentTypeOtherText) =
        update { it.copy(apartmentTypeName = type.name, apartmentTypeOtherText = otherText) }
    fun setCheckinDate(date: LocalDate?) = update { it.copy(checkinDateIso = date?.toString()) }
    fun setCheckoutDate(date: LocalDate?) = update { it.copy(checkoutDateIso = date?.toString()) }
    fun setCheckinTime(time: String) = update { it.copy(checkinTime = time) }
    fun setCheckoutTime(time: String) = update { it.copy(checkoutTime = time) }
    fun setDailyRate(rate: Long) = update { it.copy(dailyRate = rate) }
    fun setDeposit(deposit: Long) = update { it.copy(deposit = deposit) }
}

fun SharedFields.checkinDate(): LocalDate? = checkinDateIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
fun SharedFields.checkoutDate(): LocalDate? = checkoutDateIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
fun SharedFields.nights(): Int {
    val start = checkinDate()
    val end = checkoutDate()
    if (start == null || end == null) return 0
    val days = java.time.temporal.ChronoUnit.DAYS.between(start, end)
    return if (days > 0) days.toInt() else 0
}
fun SharedFields.total(): Long = deposit + dailyRate * nights()
