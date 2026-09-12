package com.hrhousing.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrhousing.app.AppContainer
import com.hrhousing.app.ui.AppViewModelFactory
import com.hrhousing.app.ui.theme.heatColor
import com.hrhousing.app.util.TimeUtils

@Composable
fun DashboardScreen(container: AppContainer) {
    val viewModel: DashboardViewModel = viewModel(factory = AppViewModelFactory(container))
    val residing by viewModel.residing.collectAsState()
    val heat by viewModel.cityHeat.collectAsState()
    val isDark by container.themePreferences.isDarkTheme.collectAsState()
    val today = TimeUtils.today()
    val cities = residing.map { it.city }.filter { it.isNotBlank() }.distinct()

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Кто сейчас проживает и в каком городе", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Сейчас проживает: ${residing.size} чел. в ${cities.size} городах",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(12.dp))
        }

        item {
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                Column {
                    HeaderRow()
                    Divider()
                    residing.forEach { row -> ResidingRowView(row, today) }
                }
            }
            Spacer(Modifier.height(28.dp))
        }

        item {
            Text("Тепловая карта загруженности по городам", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
        }

        item {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier.height((((heat.size + 1) / 2) * 100).dp.coerceAtLeast(100.dp)),
            ) {
                items(heat) { cityHeat ->
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(width = 130.dp, height = 90.dp)
                            .background(heatColor(cityHeat.step, isDark), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                    ) {
                        Column {
                            Text(cityHeat.city, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            Spacer(Modifier.height(4.dp))
                            Text("${cityHeat.count}", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            HeatLegend(isDark)
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HeaderRow() {
    Row {
        listOf("ФИО" to 160, "Департамент" to 160, "Город" to 120, "Комнат" to 90, "Заселён с" to 120, "Выселяется" to 120)
            .forEach { (label, width) ->
                Text(label, modifier = Modifier.width(width.dp).padding(8.dp), style = MaterialTheme.typography.labelLarge)
            }
    }
}

@Composable
private fun ResidingRowView(row: ResidingRow, today: java.time.LocalDate) {
    val isCheckoutToday = row.checkout == today
    Row {
        Text(row.name, modifier = Modifier.width(160.dp).padding(8.dp))
        Text(row.department, modifier = Modifier.width(160.dp).padding(8.dp))
        Text(row.city, modifier = Modifier.width(120.dp).padding(8.dp))
        Text(row.rooms?.toString() ?: "—", modifier = Modifier.width(90.dp).padding(8.dp))
        Text(TimeUtils.format(row.checkin), modifier = Modifier.width(120.dp).padding(8.dp))
        Text(
            TimeUtils.format(row.checkout),
            modifier = Modifier.width(120.dp).padding(8.dp),
            color = if (isCheckoutToday) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            style = if (isCheckoutToday) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyMedium,
        )
    }
    Divider()
}

@Composable
private fun HeatLegend(isDark: Boolean) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text("Меньше", style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.width(8.dp))
        for (step in 0..4) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(heatColor(step, isDark), RoundedCornerShape(4.dp)),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text("Больше", style = MaterialTheme.typography.labelSmall)
    }
}
