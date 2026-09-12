package com.hrhousing.app.ui.screens.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.db.entity.ApartmentType
import com.hrhousing.app.data.db.entity.FinanceRegistryEntryEntity
import com.hrhousing.app.data.db.entity.label
import com.hrhousing.app.data.prefs.TemplateKey
import com.hrhousing.app.data.shared.SharedFields
import com.hrhousing.app.data.shared.checkinDate
import com.hrhousing.app.data.shared.checkoutDate
import com.hrhousing.app.data.shared.nights
import com.hrhousing.app.data.shared.total
import com.hrhousing.app.util.TemplateEngine
import com.hrhousing.app.util.TimeUtils
import com.hrhousing.app.util.formatMoney
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** An extra (non-main) approval object; the main object mirrors the app-wide synced fields instead. */
data class ExtraFinanceObject(
    val localId: Long,
    val city: String = "",
    val fullName: String = "",
    val apartmentType: ApartmentType = ApartmentType.ONE_ROOM,
    val apartmentTypeOtherText: String = "",
    val guestsCount: Int = 1,
    val checkinDate: LocalDate? = null,
    val checkoutDate: LocalDate? = null,
    val dailyRate: Long = 0,
    val deposit: Long = 0,
    val comment: String = "",
) {
    val nights: Int
        get() {
            val s = checkinDate; val e = checkoutDate
            if (s == null || e == null) return 0
            val d = ChronoUnit.DAYS.between(s, e)
            return if (d > 0) d.toInt() else 0
        }
    val total: Long get() = deposit + dailyRate * nights
}

class FinanceApprovalViewModel(private val container: AppContainer) : ViewModel() {
    val sharedFields: StateFlow<SharedFields> = container.sharedFieldsRepository.fields
    val template: StateFlow<String> = container.templateStore.observe(TemplateKey.FINANCE_OBJECT)

    private val _extraObjects = MutableStateFlow<List<ExtraFinanceObject>>(emptyList())
    val extraObjects: StateFlow<List<ExtraFinanceObject>> = _extraObjects

    private var nextLocalId = 1L

    val cityOptions: StateFlow<List<String>> = container.cityRepository.observeAll()
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val registry: StateFlow<List<FinanceRegistryEntryEntity>> = container.financeRegistryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _checkedRegistryIds = MutableStateFlow<Set<Long>>(emptySet())
    val checkedRegistryIds: StateFlow<Set<Long>> = _checkedRegistryIds

    val mainGuestsCount: StateFlow<Int> = sharedFields.map { it.guestNames.count { n -> n.isNotBlank() }.coerceAtLeast(1) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val generatedText: StateFlow<String> = combine(sharedFields, _extraObjects, template, mainGuestsCount) { shared, extras, tmpl, mainGuests ->
        val blocks = mutableListOf<String>()
        blocks.add(renderMainObject(shared, tmpl, mainGuests))
        extras.forEach { obj -> blocks.add(renderExtraObject(obj, tmpl)) }
        var text = blocks.joinToString("\n\n———\n\n")
        if (extras.isNotEmpty()) {
            val grandTotal = shared.total() + extras.sumOf { it.total }
            text += "\n\nИтоговая сумма: ${grandTotal.formatMoney()}"
        }
        text
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private fun renderMainObject(shared: SharedFields, tmpl: String, guestsCount: Int): String {
        val apartmentTypeLabel = if (shared.apartmentType == ApartmentType.OTHER) shared.apartmentTypeOtherText else shared.apartmentType.label()
        val tokens = mapOf(
            "город" to shared.city,
            "фио" to shared.guestNames.filter { it.isNotBlank() }.joinToString(", "),
            "тип_квартиры" to apartmentTypeLabel,
            "гостей" to guestsCount.toString(),
            "дата_заезда" to TimeUtils.format(shared.checkinDate()),
            "дата_выезда" to TimeUtils.format(shared.checkoutDate()),
            "ставка" to shared.dailyRate.formatMoney(),
            "депозит" to shared.deposit.formatMoney(),
            "итого" to shared.total().formatMoney(),
            "комментарий" to "",
        )
        return TemplateEngine.render(tmpl, tokens)
    }

    private fun renderExtraObject(obj: ExtraFinanceObject, tmpl: String): String {
        val apartmentTypeLabel = if (obj.apartmentType == ApartmentType.OTHER) obj.apartmentTypeOtherText else obj.apartmentType.label()
        val tokens = mapOf(
            "город" to obj.city,
            "фио" to obj.fullName,
            "тип_квартиры" to apartmentTypeLabel,
            "гостей" to obj.guestsCount.toString(),
            "дата_заезда" to TimeUtils.format(obj.checkinDate),
            "дата_выезда" to TimeUtils.format(obj.checkoutDate),
            "ставка" to obj.dailyRate.formatMoney(),
            "депозит" to obj.deposit.formatMoney(),
            "итого" to obj.total.formatMoney(),
            "комментарий" to obj.comment,
        )
        return TemplateEngine.render(tmpl, tokens)
    }

    fun updateTemplate(text: String) = container.templateStore.update(viewModelScope, TemplateKey.FINANCE_OBJECT, text)

    fun addExtraObject() {
        _extraObjects.value = _extraObjects.value + ExtraFinanceObject(localId = nextLocalId++)
    }

    fun removeExtraObject(localId: Long) {
        _extraObjects.value = _extraObjects.value.filterNot { it.localId == localId }
    }

    fun updateExtraObject(localId: Long, transform: (ExtraFinanceObject) -> ExtraFinanceObject) {
        _extraObjects.value = _extraObjects.value.map { if (it.localId == localId) transform(it) else it }
    }

    // main object setters (write straight through to the shared bus)
    fun setCity(v: String) = container.sharedFieldsRepository.setCity(v)
    fun setApartmentType(type: ApartmentType, other: String) = container.sharedFieldsRepository.setApartmentType(type, other)
    fun setCheckinDate(d: LocalDate?) = container.sharedFieldsRepository.setCheckinDate(d)
    fun setCheckoutDate(d: LocalDate?) = container.sharedFieldsRepository.setCheckoutDate(d)
    fun setDailyRate(v: Long) = container.sharedFieldsRepository.setDailyRate(v)
    fun setDeposit(v: Long) = container.sharedFieldsRepository.setDeposit(v)

    fun saveToRegistry() {
        viewModelScope.launch {
            val shared = sharedFields.value
            val guests = mainGuestsCount.value
            container.financeRegistryRepository.upsert(
                FinanceRegistryEntryEntity(
                    city = shared.city,
                    fullName = shared.guestNames.filter { it.isNotBlank() }.joinToString(", "),
                    apartmentType = shared.apartmentType,
                    apartmentTypeOtherText = shared.apartmentTypeOtherText,
                    guestsCount = guests,
                    checkinDate = shared.checkinDate(),
                    checkoutDate = shared.checkoutDate(),
                    nights = shared.nights(),
                    dailyRate = shared.dailyRate,
                    deposit = shared.deposit,
                    total = shared.total(),
                    comment = "",
                ),
            )
            _extraObjects.value.forEach { obj ->
                container.financeRegistryRepository.upsert(
                    FinanceRegistryEntryEntity(
                        city = obj.city,
                        fullName = obj.fullName,
                        apartmentType = obj.apartmentType,
                        apartmentTypeOtherText = obj.apartmentTypeOtherText,
                        guestsCount = obj.guestsCount,
                        checkinDate = obj.checkinDate,
                        checkoutDate = obj.checkoutDate,
                        nights = obj.nights,
                        dailyRate = obj.dailyRate,
                        deposit = obj.deposit,
                        total = obj.total,
                        comment = obj.comment,
                    ),
                )
            }
        }
    }

    fun toggleRegistryChecked(id: Long) {
        _checkedRegistryIds.value = if (id in _checkedRegistryIds.value) _checkedRegistryIds.value - id else _checkedRegistryIds.value + id
    }

    fun deleteRegistryEntry(entry: FinanceRegistryEntryEntity) {
        viewModelScope.launch { container.financeRegistryRepository.delete(entry) }
    }

    /** "➕ Добавить выбранные строки как объекты": first checked row fills the main object if it's empty, the rest become extra objects. */
    fun addCheckedRowsAsObjects() {
        val checked = registry.value.filter { it.id in _checkedRegistryIds.value }
        if (checked.isEmpty()) return
        var remaining = checked
        if (sharedFields.value.city.isBlank() && sharedFields.value.guestNames.all { it.isBlank() }) {
            val first = checked.first()
            container.sharedFieldsRepository.update {
                it.copy(
                    city = first.city,
                    guestNames = first.fullName.split(",").map { n -> n.trim() }.filter { n -> n.isNotEmpty() }.ifEmpty { listOf("") },
                    apartmentTypeName = first.apartmentType.name,
                    apartmentTypeOtherText = first.apartmentTypeOtherText,
                    checkinDateIso = first.checkinDate?.toString(),
                    checkoutDateIso = first.checkoutDate?.toString(),
                    dailyRate = first.dailyRate,
                    deposit = first.deposit,
                )
            }
            remaining = checked.drop(1)
        }
        val newObjects = remaining.map { entry ->
            ExtraFinanceObject(
                localId = nextLocalId++,
                city = entry.city,
                fullName = entry.fullName,
                apartmentType = entry.apartmentType,
                apartmentTypeOtherText = entry.apartmentTypeOtherText,
                guestsCount = entry.guestsCount,
                checkinDate = entry.checkinDate,
                checkoutDate = entry.checkoutDate,
                dailyRate = entry.dailyRate,
                deposit = entry.deposit,
                comment = entry.comment,
            )
        }
        _extraObjects.value = _extraObjects.value + newObjects
        _checkedRegistryIds.value = emptySet()
    }
}
