package com.hrhousing.app.ui.screens.database

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.db.entity.AIRPORT_CODES
import com.hrhousing.app.data.db.entity.ApartmentType
import com.hrhousing.app.data.db.entity.DEPARTMENTS
import com.hrhousing.app.data.db.entity.DepositReturnStatus
import com.hrhousing.app.data.db.entity.PaymentMethod
import com.hrhousing.app.data.db.entity.Periodicity
import com.hrhousing.app.data.db.entity.RentalRecordEntity
import com.hrhousing.app.data.db.entity.TripPurposeType
import com.hrhousing.app.data.db.entity.label
import com.hrhousing.app.ui.AppViewModelFactory
import com.hrhousing.app.ui.components.AutosuggestTextField
import com.hrhousing.app.ui.components.ConfirmDeleteAllDialog
import com.hrhousing.app.ui.components.DateField
import com.hrhousing.app.ui.components.ExpandableCell
import com.hrhousing.app.ui.components.MoneyField
import com.hrhousing.app.ui.components.TilePicker
import com.hrhousing.app.util.TimeUtils
import com.hrhousing.app.util.formatMoney

private const val XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

@Composable
fun RentalDatabaseScreen(container: AppContainer) {
    val viewModel: RentalDatabaseViewModel = viewModel(factory = AppViewModelFactory(container))
    val draft by viewModel.draft.collectAsState()
    val records by viewModel.records.collectAsState()
    val showAll by viewModel.showAll.collectAsState()
    val status by viewModel.statusMessage.collectAsState()
    val cityFlow = remember(container) { container.cityRepository.observeAll() }
    val cityOptions by cityFlow.collectAsState(initial = emptyList())
    val context = LocalContext.current
    var showDeleteAll by remember { mutableStateOf(false) }

    var pendingReceiptSource by remember { mutableStateOf<android.net.Uri?>(null) }
    var showReceiptDescriptionDialog by remember { mutableStateOf(false) }
    var receiptDescriptionText by remember { mutableStateOf("") }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importFromFile(context, it) }
    }
    val importCorporateLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importCorporateFormat(context, it) }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(XLSX_MIME)) { uri ->
        uri?.let { viewModel.exportToExcel(context, it) }
    }
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(XLSX_MIME)) { uri ->
        uri?.let { viewModel.exportFullBackup(context, it) }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.restoreFromBackup(context, it) }
    }
    val pickReceiptLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pendingReceiptSource = uri
            showReceiptDescriptionDialog = true
        }
    }
    val createReceiptDestLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { destUri ->
        val source = pendingReceiptSource
        if (destUri != null && source != null) {
            viewModel.attachReceipt(context, source, receiptDescriptionText, destUri)
        }
        pendingReceiptSource = null
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        item {
            Text("Новая запись об аренде", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            EnumDropdown("Код аэропорта", draft.airportCode, AIRPORT_CODES) { v -> viewModel.updateDraft { it.copy(airportCode = v) } }
            Spacer(Modifier.height(8.dp))
            AutosuggestTextField("Город", draft.city, { v -> viewModel.updateDraft { it.copy(city = v) } }, cityOptions.map { it.name }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            Text("Периодичность", style = MaterialTheme.typography.titleSmall)
            TilePicker(Periodicity.values().toList(), draft.periodicity, { v -> viewModel.updateDraft { it.copy(periodicity = v) } }, {
                if (it == Periodicity.DAILY) "Ежедневно" else "Ежемесячно"
            })
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateField("Дата начала аренды", draft.rentStartDate, { d -> viewModel.updateDraft { it.copy(rentStartDate = d) }; viewModel.recomputeDerived() }, Modifier.weight(1f))
                DateField("Дата завершения аренды", draft.rentEndDate, { d -> viewModel.updateDraft { it.copy(rentEndDate = d) }; viewModel.recomputeDerived() }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text("Срок аренды: ${draft.rentDurationDays} дн.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))

            MoneyField("Сумма аренды", draft.rentAmount, { v -> viewModel.updateDraft { it.copy(rentAmount = v) }; viewModel.recomputeDerived() }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text("Итого сумма аренды: ${draft.rentTotalAmount.formatMoney()}", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            DateField("Дата оплаты аренды", draft.rentPaymentDate, { d -> viewModel.updateDraft { it.copy(rentPaymentDate = d) } }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            MoneyField("Сумма депозита", draft.depositAmount, { v -> viewModel.updateDraft { it.copy(depositAmount = v) } }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text("Статус возврата депозита", style = MaterialTheme.typography.titleSmall)
            TilePicker(DepositReturnStatus.values().toList(), draft.depositReturnStatus, { v -> viewModel.updateDraft { it.copy(depositReturnStatus = v) } }, {
                when (it) {
                    DepositReturnStatus.NONE -> "(не указано)"
                    DepositReturnStatus.RETURNED -> "Вернули"
                    DepositReturnStatus.WITH_LANDLORD -> "У арендатора"
                }
            })
            Spacer(Modifier.height(12.dp))

            EnumDropdown("Департамент", draft.department, DEPARTMENTS) { v -> viewModel.updateDraft { it.copy(department = v) } }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(draft.fullName, { v -> viewModel.updateDraft { it.copy(fullName = v) } }, label = { Text("ФИО") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            Text("Цель поездки", style = MaterialTheme.typography.titleSmall)
            TilePicker(TripPurposeType.values().toList(), draft.purposeType, { v -> viewModel.updateDraft { it.copy(purposeType = v) } }, {
                if (it == TripPurposeType.WAREHOUSE_OPENING) "В рамках открытия складов" else "Другое"
            })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(draft.purposeOtherText, { v -> viewModel.updateDraft { it.copy(purposeOtherText = v) } }, label = { Text("Цель поездки (другое)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            Text("Тип квартиры", style = MaterialTheme.typography.titleSmall)
            TilePicker(ApartmentType.values().toList(), draft.apartmentType, { v -> viewModel.setApartmentType(v, draft.apartmentTypeOtherText) }, { it.label() })
            if (draft.apartmentType == ApartmentType.OTHER) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(draft.apartmentTypeOtherText, { v -> viewModel.setApartmentType(ApartmentType.OTHER, v) }, label = { Text("Тип (другое)") }, modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                if (draft.roomsCount == 0) "" else draft.roomsCount.toString(),
                { v -> viewModel.updateDraft { it.copy(roomsCount = v.filter { c -> c.isDigit() }.toIntOrNull() ?: 0) } },
                label = { Text("Кол-во комнат") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                if (draft.guestsCount == 0) "" else draft.guestsCount.toString(),
                { v -> viewModel.updateDraft { it.copy(guestsCount = v.filter { c -> c.isDigit() }.toIntOrNull() ?: 0) } },
                label = { Text("Сколько проживает гостей") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            MoneyField("Экономия при 2-местном размещении", draft.savingsTwoPerson, { v -> viewModel.updateDraft { it.copy(savingsTwoPerson = v) } }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            MoneyField("Экономия при 1-местном размещении", draft.savingsOnePerson, { v -> viewModel.updateDraft { it.copy(savingsOnePerson = v) } }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(draft.address, { v -> viewModel.updateDraft { it.copy(address = v) } }, label = { Text("Адрес") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            MoneyField("Квартплата", draft.utilitiesAmount, { v -> viewModel.updateDraft { it.copy(utilitiesAmount = v) } }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))

            Card {
                Column(Modifier.padding(12.dp)) {
                    Text("Контакты арендодателя", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(draft.landlordFullName, { v -> viewModel.updateDraft { it.copy(landlordFullName = v) } }, label = { Text("ФИО арендодателя") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(draft.landlordPhone, { v -> viewModel.updateDraft { it.copy(landlordPhone = v) } }, label = { Text("Телефон") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    TilePicker(PaymentMethod.values().toList(), draft.landlordPaymentMethod, { v -> viewModel.updateDraft { it.copy(landlordPaymentMethod = v) } }, {
                        if (it == PaymentMethod.INVOICE) "Счёт" else "Перевод"
                    })
                    Spacer(Modifier.height(8.dp))
                    Text("Рейтинг арендодателя", style = MaterialTheme.typography.labelLarge)
                    TilePicker((1..10).toList(), draft.landlordRating, { v -> viewModel.updateDraft { it.copy(landlordRating = v) } }, { it.toString() })
                }
            }
            Spacer(Modifier.height(16.dp))

            Card {
                Column(Modifier.padding(12.dp)) {
                    Text("Чек", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    if (draft.receiptFileName.isNotBlank()) {
                        Text("Файл: ${draft.receiptFileName}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                    }
                    OutlinedButton(onClick = { pickReceiptLauncher.launch(arrayOf("*/*")) }) { Text("Прикрепить чек") }
                }
            }
            Spacer(Modifier.height(20.dp))

            Button(onClick = viewModel::saveDraft, modifier = Modifier.fillMaxWidth()) { Text("Сохранить запись") }
            status?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            Text("Записи (${records.size})", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }

        items(if (showAll) records else records.take(5)) { record ->
            RentalRecordCard(record, onDelete = { viewModel.deleteRecord(record) })
            Spacer(Modifier.height(8.dp))
        }

        item {
            if (records.size > 5) {
                TextButton(onClick = viewModel::toggleShowAll) {
                    Text(if (showAll) "Показать только последние 5" else "Показать все записи (${records.size})")
                }
            }
            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            Text("Импорт / экспорт", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { importLauncher.launch(arrayOf(XLSX_MIME, "text/csv", "text/comma-separated-values", "*/*")) }, modifier = Modifier.fillMaxWidth()) {
                Text("Импорт из Excel/CSV")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { importCorporateLauncher.launch(arrayOf(XLSX_MIME, "*/*")) }, modifier = Modifier.fillMaxWidth()) {
                Text("Импорт из корпоративной системы")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { exportLauncher.launch("База данных аренды.xlsx") }, modifier = Modifier.fillMaxWidth()) {
                Text("Скачать Excel")
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = { backupLauncher.launch("Резервная копия.xlsx") }, modifier = Modifier.fillMaxWidth()) {
                Text("Резервное копирование")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { restoreLauncher.launch(arrayOf(XLSX_MIME, "*/*")) }, modifier = Modifier.fillMaxWidth()) {
                Text("Восстановить из копии")
            }
            Spacer(Modifier.height(24.dp))

            androidx.compose.material3.Button(
                onClick = { showDeleteAll = true },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("🗑️ Удалить все данные")
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDeleteAll) {
        ConfirmDeleteAllDialog(
            onConfirm = { viewModel.deleteAllData(); showDeleteAll = false },
            onDismiss = { showDeleteAll = false },
        )
    }

    if (showReceiptDescriptionDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showReceiptDescriptionDialog = false },
            title = { Text("Что это за чек?") },
            text = {
                OutlinedTextField(
                    value = receiptDescriptionText,
                    onValueChange = { receiptDescriptionText = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(onClick = {
                    showReceiptDescriptionDialog = false
                    val ext = pendingReceiptSource?.let { com.hrhousing.app.util.FileUtils.extensionOf(com.hrhousing.app.util.FileUtils.displayName(context, it)) } ?: ""
                    val suggested = viewModel.buildReceiptFileName(receiptDescriptionText, ext)
                    createReceiptDestLauncher.launch(suggested)
                }) { Text("Далее — выбрать папку") }
            },
            dismissButton = { TextButton(onClick = { showReceiptDescriptionDialog = false }) { Text("Отмена") } },
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun EnumDropdown(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.material3.ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

@Composable
private fun RentalRecordCard(record: RentalRecordEntity, onDelete: () -> Unit) {
    Card {
        Column(Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("${record.city} · ${record.fullName.ifBlank { "—" }}", style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Close, contentDescription = "Удалить") }
            }
            Text("${TimeUtils.format(record.rentStartDate)} – ${TimeUtils.format(record.rentEndDate)} (${record.rentDurationDays} дн.)", style = MaterialTheme.typography.bodySmall)
            ExpandableCell(record.address)
            Text("${record.apartmentType.label()} · ${record.rentAmount.formatMoney()}/сутки · Итого ${record.rentTotalAmount.formatMoney()}", style = MaterialTheme.typography.bodySmall)
            Text("Депозит: ${record.depositAmount.formatMoney()}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
