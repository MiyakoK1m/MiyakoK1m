package com.hrhousing.app.data.shared

import com.hrhousing.app.data.db.entity.ApartmentType
import com.hrhousing.app.data.net.ApiClient
import com.hrhousing.app.data.net.toJson
import com.hrhousing.app.data.net.toSharedFields
import com.hrhousing.app.data.prefs.JsonPrefStore
import com.hrhousing.app.data.prefs.ServerSettings
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
 * (see «Сквозная синхронизация полей»), without requiring the user to save any form. Backed by
 * DataStore for local persistence, and — when a server URL is configured in Settings — mirrored
 * to the web companion's `/api/shared-fields` so the same edit shows up on the website (and
 * anything typed there shows up here on the next refresh).
 */
class SharedFieldsRepository(
    private val jsonPrefStore: JsonPrefStore,
    private val serverSettings: ServerSettings,
    private val scope: CoroutineScope,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val _fields = MutableStateFlow(SharedFields())
    val fields: StateFlow<SharedFields> = _fields

    init {
        scope.launch {
            jsonPrefStore.observeRaw(PREF_KEY).first()?.let { raw ->
                runCatching { json.decodeFromString<SharedFields>(raw) }.getOrNull()?.let { _fields.value = it }
            }
            refresh()
        }
    }

    private fun base(): String? = serverSettings.baseUrl.value.ifBlank { null }

    /** Pulls the server's copy of the shared fields; a no-op when no server is configured. */
    suspend fun refresh() {
        val url = base() ?: return
        val remote = runCatching { ApiClient.getObject(url, "/api/shared-fields") }.getOrNull() ?: return
        val value = remote.toSharedFields()
        _fields.value = value
        persistLocal(value)
    }

    private fun persistLocal(value: SharedFields) {
        scope.launch { jsonPrefStore.writeRaw(PREF_KEY, json.encodeToString(value)) }
    }

    fun update(transform: (SharedFields) -> SharedFields) {
        val next = transform(_fields.value)
        _fields.value = next
        persistLocal(next)
        val url = base()
        if (url != null) {
            scope.launch { runCatching { ApiClient.putObject(url, "/api/shared-fields", next.toJson()) } }
        }
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
