package com.hrhousing.app.ui.screens.calendarview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.db.entity.label
import com.hrhousing.app.ui.AppViewModelFactory
import com.hrhousing.app.util.TimeUtils
import com.hrhousing.app.util.formatMoney
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(container: AppContainer) {
    val viewModel: CalendarViewModel = viewModel(factory = AppViewModelFactory(container))
    val month by viewModel.month.collectAsState()
    val badgesByDay by viewModel.badgesByDay.collectAsState()
    var selectedBadge by remember { mutableStateOf<DayBadge?>(null) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = viewModel::goToPreviousMonth) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Предыдущий месяц") }
            val ruLocale = Locale("ru")
            val monthName = month.month.getDisplayName(TextStyle.FULL, ruLocale).replaceFirstChar { it.uppercase() }
            Text("$monthName ${month.year}", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = viewModel::goToNextMonth) { Icon(Icons.Filled.ChevronRight, contentDescription = "Следующий месяц") }
        }
        TextButton(onClick = viewModel::goToToday, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Сегодня") }
        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth()) {
            listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                .forEach { dow ->
                    Text(
                        dow.getDisplayName(TextStyle.SHORT, Locale("ru")).replaceFirstChar { it.uppercase() },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
        }
        Spacer(Modifier.height(4.dp))

        val firstDay = month.atDay(1)
        val leadingBlanks = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
        val totalDays = month.lengthOfMonth()
        val cells: List<LocalDate?> = List(leadingBlanks) { null } + (1..totalDays).map { month.atDay(it) }
        val rows = (cells.size + 6) / 7

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height((rows * 96).dp),
        ) {
            items(cells) { date ->
                DayCell(date, if (date != null) badgesByDay[date].orEmpty() else emptyList(), onBadgeClick = { selectedBadge = it })
            }
        }
    }

    selectedBadge?.let { badge ->
        BadgeDetailDialog(badge, onDismiss = { selectedBadge = null })
    }
}

@Composable
private fun DayCell(date: LocalDate?, badges: List<DayBadge>, onBadgeClick: (DayBadge) -> Unit) {
    val today = TimeUtils.today()
    Column(
        modifier = Modifier
            .padding(2.dp)
            .height(92.dp)
            .background(
                if (date == today) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp),
            )
            .padding(4.dp),
    ) {
        if (date != null) {
            Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelMedium)
            badges.take(3).forEach { badge ->
                val bg = when {
                    badge.isCheckoutDay -> MaterialTheme.colorScheme.error
                    badge.source == BadgeSource.RENTAL_ONLY -> Color(0xFF2E7D32)
                    else -> MaterialTheme.colorScheme.primary
                }
                val icon = when (badge.source) {
                    BadgeSource.TRIP_WITH_RENTAL -> " 🏠"
                    BadgeSource.RENTAL_ONLY -> " 🔑"
                    BadgeSource.TRIP_ONLY -> ""
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp)
                        .background(bg, RoundedCornerShape(4.dp))
                        .clickable { onBadgeClick(badge) }
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                ) {
                    Text(
                        badge.name + icon,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
            }
            if (badges.size > 3) {
                Text("+${badges.size - 3}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun BadgeDetailDialog(badge: DayBadge, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(badge.name) },
        text = {
            Column {
                badge.trip?.let { trip ->
                    Text("Командировка: ${TimeUtils.format(trip.checkinDate)} – ${TimeUtils.format(trip.checkoutDate)}")
                    Text("Город: ${trip.city}, Департамент: ${trip.department}")
                }
                badge.rental?.let { rental ->
                    Spacer(Modifier.height(8.dp))
                    Text("Аренда: ${TimeUtils.format(rental.rentStartDate)} – ${TimeUtils.format(rental.rentEndDate)}")
                    Text("Адрес: ${rental.address}")
                    Text("Тип квартиры: ${rental.apartmentType.label()}")
                    Text("Ставка: ${rental.rentAmount.formatMoney()}")
                    Text("Депозит: ${rental.depositAmount.formatMoney()}")
                    val depositStatus = when (rental.depositReturnStatus) {
                        com.hrhousing.app.data.db.entity.DepositReturnStatus.NONE -> "не указано"
                        com.hrhousing.app.data.db.entity.DepositReturnStatus.RETURNED -> "вернули"
                        com.hrhousing.app.data.db.entity.DepositReturnStatus.WITH_LANDLORD -> "у арендатора"
                    }
                    Text("Возврат депозита: $depositStatus")
                }
                if (badge.isCheckoutDay) {
                    Spacer(Modifier.height(8.dp))
                    Text("⚠ Сегодня дата выселения — напоминание вернуть депозит", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Закрыть") } },
    )
}
