package com.hrhousing.app.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableView
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(val route: String, val title: String, val icon: ImageVector) {
    TripInput("trip_input", "Входная информация", Icons.Filled.Assignment),
    Dashboard("dashboard", "Дэшборд", Icons.Filled.Dashboard),
    Calendar("calendar", "Календарь", Icons.Filled.CalendarMonth),
    CheckinInfo("checkin_info", "Информация по заселению", Icons.Filled.Home),
    Booking("booking", "Бронирование", Icons.Filled.MeetingRoom),
    FinanceApproval("finance_approval", "Согласование с финансами", Icons.Filled.AttachMoney),
    RentalDatabase("rental_database", "База данных", Icons.Filled.Storage),
    RentalInfoTable("rental_info_table", "Информация по аренде", Icons.Filled.TableView),
    Landlords("landlords", "Арендодатели", Icons.Filled.Apartment),
    Residents("residents", "Проживающие сотрудники", Icons.Filled.People),
    Cities("cities", "Города", Icons.Filled.LocationCity),
}
