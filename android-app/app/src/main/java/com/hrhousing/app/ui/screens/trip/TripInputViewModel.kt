package com.hrhousing.app.ui.screens.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.db.entity.DEPARTMENTS
import com.hrhousing.app.data.db.entity.HousingType
import com.hrhousing.app.data.db.entity.LandlordEntity
import com.hrhousing.app.data.db.entity.ResidentEmployeeEntity
import com.hrhousing.app.data.db.entity.TripEntryEntity
import com.hrhousing.app.data.db.entity.TripPurposeType
import com.hrhousing.app.data.shared.SharedFields
import com.hrhousing.app.data.shared.checkinDate
import com.hrhousing.app.data.shared.checkoutDate
import com.hrhousing.app.util.FuzzyMatch
import com.hrhousing.app.util.TimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ResidentMatchWarning(val enteredName: String, val matchedComment: String)
data class TripHistoryWarning(val name: String, val start: LocalDate?, val end: LocalDate?, val justification: String)

data class TripInputLocalState(
    val position: String = "",
    val department: String = DEPARTMENTS.first(),
    val housingType: HousingType = HousingType.APARTMENT,
    val offRegulation: Boolean = false,
    val perDiem: Long = 0,
    val urgencyReason: String = "",
    val justification: String = "",
    val purposeType: TripPurposeType = TripPurposeType.WAREHOUSE_OPENING,
    val purposeOtherText: String = "",
    val savedMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class TripInputViewModel(private val container: AppContainer) : ViewModel() {
    private val _local = MutableStateFlow(TripInputLocalState())
    val local: StateFlow<TripInputLocalState> = _local

    val sharedFields: StateFlow<SharedFields> = container.sharedFieldsRepository.fields

    val cityOptions: StateFlow<List<String>> = container.cityRepository.observeAll()
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allResidents: StateFlow<List<ResidentEmployeeEntity>> = container.residentEmployeeRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allTrips: StateFlow<List<TripEntryEntity>> = container.tripEntryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val landlordsInCity: StateFlow<List<LandlordEntity>> = sharedFields
        .flatMapLatest { fields -> container.landlordRepository.observeByCity(fields.city) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val residentWarnings: StateFlow<List<ResidentMatchWarning>> = combine(sharedFields, allResidents) { fields, residents ->
        fields.guestNames.filter { it.isNotBlank() }.mapNotNull { entered ->
            residents.firstOrNull { FuzzyMatch.sameName(it.fullName, entered) }
                ?.let { ResidentMatchWarning(it.fullName, it.comment) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tripHistoryWarnings: StateFlow<List<TripHistoryWarning>> = combine(sharedFields, allTrips) { fields, trips ->
        if (fields.city.isBlank()) return@combine emptyList<TripHistoryWarning>()
        fields.guestNames.filter { it.isNotBlank() }.flatMap { entered ->
            trips.filter { trip -> trip.city.equals(fields.city, ignoreCase = true) }
                .filter { trip -> trip.guestNames.any { FuzzyMatch.sameName(it, entered) } }
                .map { trip -> TripHistoryWarning(entered, trip.checkinDate, trip.checkoutDate, trip.justification) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateLocal(transform: (TripInputLocalState) -> TripInputLocalState) {
        _local.value = transform(_local.value)
    }

    fun setGuestNames(names: List<String>) = container.sharedFieldsRepository.setGuestNames(names)
    fun setCity(city: String) = container.sharedFieldsRepository.setCity(city)
    fun setCheckinDate(date: LocalDate?) = container.sharedFieldsRepository.setCheckinDate(date)
    fun setCheckoutDate(date: LocalDate?) = container.sharedFieldsRepository.setCheckoutDate(date)

    fun save() {
        viewModelScope.launch {
            val fields = sharedFields.value
            val l = _local.value
            container.tripEntryRepository.upsert(
                TripEntryEntity(
                    guestNames = fields.guestNames,
                    position = l.position,
                    department = l.department,
                    housingType = l.housingType,
                    offRegulation = l.offRegulation,
                    city = fields.city,
                    checkinDate = fields.checkinDate(),
                    checkoutDate = fields.checkoutDate(),
                    perDiem = l.perDiem,
                    urgencyReason = l.urgencyReason,
                    justification = l.justification,
                    purposeType = l.purposeType,
                    purposeOtherText = l.purposeOtherText,
                ),
            )
            _local.value = l.copy(savedMessage = "Сохранено: ${TimeUtils.format(TimeUtils.today())}")
        }
    }
}
