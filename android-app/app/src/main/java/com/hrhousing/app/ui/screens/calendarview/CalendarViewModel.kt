package com.hrhousing.app.ui.screens.calendarview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.db.entity.RentalRecordEntity
import com.hrhousing.app.data.db.entity.TripEntryEntity
import com.hrhousing.app.util.FuzzyMatch
import com.hrhousing.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

enum class BadgeSource { TRIP_ONLY, TRIP_WITH_RENTAL, RENTAL_ONLY }

data class DayBadge(
    val name: String,
    val source: BadgeSource,
    val isCheckoutDay: Boolean,
    val trip: TripEntryEntity?,
    val rental: RentalRecordEntity?,
)

class CalendarViewModel(container: AppContainer) : ViewModel() {
    private val _month = MutableStateFlow(YearMonth.from(TimeUtils.today()))
    val month: StateFlow<YearMonth> = _month

    private val trips = container.tripEntryRepository.observeAll()
    private val rentals = container.rentalRecordRepository.observeAll()

    val badgesByDay: StateFlow<Map<LocalDate, List<DayBadge>>> = combine(trips, rentals, _month) { tripList, rentalList, ym ->
        val result = mutableMapOf<LocalDate, MutableList<DayBadge>>()
        var day = ym.atDay(1)
        val end = ym.atEndOfMonth()
        while (!day.isAfter(end)) {
            val dayBadges = mutableListOf<DayBadge>()
            val activeTripsToday = tripList.filter { TimeUtils.isActiveToday(it.checkinDate, it.checkoutDate, day) }
            val namesFromTrips = mutableSetOf<String>()

            for (trip in activeTripsToday) {
                for (name in trip.guestNames.filter { it.isNotBlank() }) {
                    namesFromTrips.add(name.lowercase())
                    val rentalMatch = rentalList.firstOrNull { rental ->
                        FuzzyMatch.sameName(rental.fullName, name) &&
                            TimeUtils.isActiveToday(rental.rentStartDate, rental.rentEndDate, day)
                    }
                    val isCheckout = day == trip.checkoutDate
                    dayBadges.add(
                        DayBadge(
                            name = name,
                            source = if (rentalMatch != null) BadgeSource.TRIP_WITH_RENTAL else BadgeSource.TRIP_ONLY,
                            isCheckoutDay = isCheckout,
                            trip = trip,
                            rental = rentalMatch,
                        ),
                    )
                }
            }

            val activeRentalsToday = rentalList.filter { TimeUtils.isActiveToday(it.rentStartDate, it.rentEndDate, day) }
            for (rental in activeRentalsToday) {
                if (rental.fullName.isBlank()) continue
                if (namesFromTrips.contains(rental.fullName.lowercase())) continue
                val alreadyMatchedByFuzzy = namesFromTrips.any { FuzzyMatch.sameName(it, rental.fullName) }
                if (alreadyMatchedByFuzzy) continue
                dayBadges.add(
                    DayBadge(
                        name = rental.fullName,
                        source = BadgeSource.RENTAL_ONLY,
                        isCheckoutDay = day == rental.rentEndDate,
                        trip = null,
                        rental = rental,
                    ),
                )
            }

            if (dayBadges.isNotEmpty()) result[day] = dayBadges
            day = day.plusDays(1)
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun goToPreviousMonth() { _month.value = _month.value.minusMonths(1) }
    fun goToNextMonth() { _month.value = _month.value.plusMonths(1) }
    fun goToToday() { _month.value = YearMonth.from(TimeUtils.today()) }
}
