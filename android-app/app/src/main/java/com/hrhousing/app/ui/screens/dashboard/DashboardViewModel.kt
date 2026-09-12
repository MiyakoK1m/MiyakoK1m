package com.hrhousing.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrhousing.app.AppContainer
import com.hrhousing.app.util.FuzzyMatch
import com.hrhousing.app.util.TimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import kotlin.math.roundToInt

data class ResidingRow(
    val name: String,
    val department: String,
    val city: String,
    val rooms: Int?,
    val checkin: LocalDate?,
    val checkout: LocalDate?,
)

data class CityHeat(val city: String, val count: Int, val step: Int)

class DashboardViewModel(container: AppContainer) : ViewModel() {

    val residing: StateFlow<List<ResidingRow>> = combine(
        container.tripEntryRepository.observeAll(),
        container.rentalRecordRepository.observeAll(),
    ) { trips, rentals ->
        val today = TimeUtils.today()
        trips.filter { TimeUtils.isActiveToday(it.checkinDate, it.checkoutDate, today) }
            .flatMap { trip ->
                trip.guestNames.filter { it.isNotBlank() }.map { name ->
                    val matchingRental = rentals.firstOrNull { rental ->
                        FuzzyMatch.sameName(rental.fullName, name) &&
                            TimeUtils.rangesOverlap(trip.checkinDate, trip.checkoutDate, rental.rentStartDate, rental.rentEndDate)
                    }
                    ResidingRow(
                        name = name,
                        department = trip.department,
                        city = trip.city,
                        rooms = matchingRental?.roomsCount,
                        checkin = trip.checkinDate,
                        checkout = trip.checkoutDate,
                    )
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cityHeat: StateFlow<List<CityHeat>> = residing.map { rows ->
        val byCity = rows.groupingBy { it.city }.eachCount().filterKeys { it.isNotBlank() }
        val maxCount = byCity.values.maxOrNull() ?: 0
        byCity.entries.sortedByDescending { it.value }.map { (city, count) ->
            val step = if (maxCount == 0) 0 else ((count.toDouble() / maxCount) * 4).roundToInt().coerceIn(0, 4)
            CityHeat(city, count, step)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
