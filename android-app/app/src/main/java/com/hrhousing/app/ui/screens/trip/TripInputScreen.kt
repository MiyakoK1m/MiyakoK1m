package com.hrhousing.app.ui.screens.trip

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.db.entity.DEPARTMENTS
import com.hrhousing.app.data.db.entity.HousingType
import com.hrhousing.app.data.db.entity.LandlordEntity
import com.hrhousing.app.data.db.entity.TripPurposeType
import com.hrhousing.app.data.shared.checkinDate
import com.hrhousing.app.data.shared.checkoutDate
import com.hrhousing.app.ui.AppViewModelFactory
import com.hrhousing.app.ui.components.AutosuggestTextField
import com.hrhousing.app.ui.components.DateField
import com.hrhousing.app.ui.components.DynamicNameList
import com.hrhousing.app.ui.components.MoneyField
import com.hrhousing.app.ui.components.TilePicker
import com.hrhousing.app.util.TimeUtils

@Composable
fun TripInputScreen(container: AppContainer) {
    val viewModel: TripInputViewModel = viewModel(factory = AppViewModelFactory(container))
    val local by viewModel.local.collectAsState()
    val shared by viewModel.sharedFields.collectAsState()
    val cityOptions by viewModel.cityOptions.collectAsState()
    val residentWarnings by viewModel.residentWarnings.collectAsState()
    val historyWarnings by viewModel.tripHistoryWarnings.collectAsState()
    val landlords by viewModel.landlordsInCity.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        item {
            Text("ФИО проживающих", style = MaterialTheme.typography.titleSmall)
            DynamicNameList(names = shared.guestNames, onChange = viewModel::setGuestNames)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = local.position,
                onValueChange = { v -> viewModel.updateLocal { it.copy(position = v) } },
                label = { Text("Должность") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            DepartmentDropdown(local.department) { d -> viewModel.updateLocal { it.copy(department = d) } }
            Spacer(Modifier.height(16.dp))

            Text("Вид жилья", style = MaterialTheme.typography.titleSmall)
            TilePicker(
                options = listOf(HousingType.APARTMENT, HousingType.HOTEL),
                selected = local.housingType,
                onSelect = { v -> viewModel.updateLocal { it.copy(housingType = v) } },
                label = { if (it == HousingType.APARTMENT) "Квартира" else "Отель" },
            )
            Spacer(Modifier.height(16.dp))

            Text("Командировка вне регламента", style = MaterialTheme.typography.titleSmall)
            TilePicker(
                options = listOf(false, true),
                selected = local.offRegulation,
                onSelect = { v -> viewModel.updateLocal { it.copy(offRegulation = v) } },
                label = { if (it) "Да" else "Нет" },
                highlightRed = { it },
            )
            Spacer(Modifier.height(16.dp))

            AutosuggestTextField(
                label = "Город назначения",
                value = shared.city,
                onChange = viewModel::setCity,
                options = cityOptions,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateField("Дата заселения", shared.checkinDate(), viewModel::setCheckinDate, Modifier.weight(1f))
                DateField("Дата выселения", shared.checkoutDate(), viewModel::setCheckoutDate, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))

            MoneyField("Сумма суточных", local.perDiem, { v -> viewModel.updateLocal { it.copy(perDiem = v) } }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = local.urgencyReason,
                onValueChange = { v -> viewModel.updateLocal { it.copy(urgencyReason = v) } },
                label = { Text("Причина срочности") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = local.justification,
                onValueChange = { v -> viewModel.updateLocal { it.copy(justification = v) } },
                label = { Text("Обоснование командировки") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
            Spacer(Modifier.height(16.dp))

            Card {
                Column(Modifier.padding(12.dp)) {
                    Text("Цель поездки", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    TilePicker(
                        options = listOf(TripPurposeType.WAREHOUSE_OPENING, TripPurposeType.OTHER),
                        selected = local.purposeType,
                        onSelect = { v -> viewModel.updateLocal { it.copy(purposeType = v) } },
                        label = { if (it == TripPurposeType.WAREHOUSE_OPENING) "В рамках открытия складов" else "Другое" },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            Card {
                Column(Modifier.padding(12.dp)) {
                    Text("Цель поездки (другое)", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = local.purposeOtherText,
                        onValueChange = { v -> viewModel.updateLocal { it.copy(purposeOtherText = v) } },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Развёрнутая альтернативная формулировка, если нужна") },
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            if (residentWarnings.isNotEmpty()) {
                WarningPanel("Проверка по списку \"Проживающие сотрудники\"") {
                    residentWarnings.forEach { w ->
                        Text("⚠ ${w.enteredName} — ${w.matchedComment}", color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (historyWarnings.isNotEmpty()) {
                WarningPanel("История командировок в этот город") {
                    historyWarnings.forEach { w ->
                        Text(
                            "⚠ ${w.name} уже был(а) в командировке в этом городе: ${TimeUtils.format(w.start)} – ${TimeUtils.format(w.end)} — «${w.justification}»",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (shared.city.isNotBlank() && landlords.isNotEmpty()) {
                Text("Арендодатели в этом городе", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
            }
        }

        items(if (shared.city.isNotBlank()) landlords else emptyList()) { landlord ->
            LandlordSuggestionCard(landlord, shared.guestNames, shared.city, shared.checkinDate(), shared.checkoutDate())
            Spacer(Modifier.height(8.dp))
        }

        item {
            Spacer(Modifier.height(12.dp))
            Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) {
                Text("Сохранить командировку")
            }
            local.savedMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DepartmentDropdown(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Департамент") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DEPARTMENTS.forEach { dept ->
                DropdownMenuItem(text = { Text(dept) }, onClick = { onSelect(dept); expanded = false })
            }
        }
    }
}

@Composable
private fun WarningPanel(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun LandlordSuggestionCard(
    landlord: LandlordEntity,
    guestNames: List<String>,
    city: String,
    checkin: java.time.LocalDate?,
    checkout: java.time.LocalDate?,
) {
    val context = LocalContext.current
    val badgeColor = when {
        landlord.rating >= 7 -> Color(0xFF2E7D32)
        landlord.rating >= 4 -> Color(0xFFF9A825)
        else -> Color(0xFFC62828)
    }
    Card {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(landlord.fullName, style = MaterialTheme.typography.bodyLarge)
                Text(landlord.phone, style = MaterialTheme.typography.bodySmall)
                Text(landlord.comment, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .background(badgeColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text("★ ${landlord.rating}", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = {
                    val names = guestNames.filter { it.isNotBlank() }.joinToString(", ")
                    val message = "Здравствуйте! По поводу аренды в г. $city для $names, заезд ${TimeUtils.format(checkin)}, выезд ${TimeUtils.format(checkout)}."
                    val phoneDigits = landlord.phone.filter { it.isDigit() }
                    val url = "https://wa.me/$phoneDigits?text=${Uri.encode(message)}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }) {
                    Text("WhatsApp")
                }
            }
        }
    }
}
