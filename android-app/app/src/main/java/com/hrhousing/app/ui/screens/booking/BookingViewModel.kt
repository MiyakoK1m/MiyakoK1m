package com.hrhousing.app.ui.screens.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.db.entity.ApartmentType
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class BookingViewModel(private val container: AppContainer) : ViewModel() {
    val sharedFields: StateFlow<SharedFields> = container.sharedFieldsRepository.fields
    val template: StateFlow<String> = container.templateStore.observe(TemplateKey.BOOKING)
    val cityOptions: StateFlow<List<String>> = container.cityRepository.observeAll()
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val generatedText: StateFlow<String> = combine(sharedFields, template) { shared, tmpl ->
        val apartmentTypeLabel = if (shared.apartmentType == ApartmentType.OTHER) shared.apartmentTypeOtherText else shared.apartmentType.label()
        val tokens = mapOf(
            "фио_гостей" to shared.guestNames.filter { it.isNotBlank() }.joinToString(", "),
            "город" to shared.city,
            "жк" to shared.complexName,
            "адрес" to shared.address,
            "тип_квартиры" to apartmentTypeLabel,
            "дата_заезда" to TimeUtils.format(shared.checkinDate()),
            "дата_выезда" to TimeUtils.format(shared.checkoutDate()),
            "время_заезда" to shared.checkinTime,
            "время_выезда" to shared.checkoutTime,
            "ставка" to shared.dailyRate.formatMoney(),
            "депозит" to shared.deposit.formatMoney(),
            "итого" to shared.total().formatMoney(),
        )
        TemplateEngine.render(tmpl, tokens)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun updateTemplate(text: String) = container.templateStore.update(viewModelScope, TemplateKey.BOOKING, text)

    fun setGuestNames(names: List<String>) = container.sharedFieldsRepository.setGuestNames(names)
    fun setCity(v: String) = container.sharedFieldsRepository.setCity(v)
    fun setComplexName(v: String) = container.sharedFieldsRepository.setComplexName(v)
    fun setAddress(v: String) = container.sharedFieldsRepository.setAddress(v)
    fun setApartmentType(type: ApartmentType, other: String) = container.sharedFieldsRepository.setApartmentType(type, other)
    fun setCheckinDate(date: LocalDate?) = container.sharedFieldsRepository.setCheckinDate(date)
    fun setCheckoutDate(date: LocalDate?) = container.sharedFieldsRepository.setCheckoutDate(date)
    fun setCheckinTime(v: String) = container.sharedFieldsRepository.setCheckinTime(v)
    fun setCheckoutTime(v: String) = container.sharedFieldsRepository.setCheckoutTime(v)
    fun setDailyRate(v: Long) = container.sharedFieldsRepository.setDailyRate(v)
    fun setDeposit(v: Long) = container.sharedFieldsRepository.setDeposit(v)
}
