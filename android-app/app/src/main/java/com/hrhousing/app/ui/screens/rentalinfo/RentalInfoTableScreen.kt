package com.hrhousing.app.ui.screens.rentalinfo

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.db.entity.RentalRecordEntity
import com.hrhousing.app.ui.AppViewModelFactory
import com.hrhousing.app.ui.components.AutosuggestTextField
import com.hrhousing.app.util.excel.ExcelService

private const val XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

@Composable
fun RentalInfoTableScreen(container: AppContainer) {
    val viewModel: RentalInfoTableViewModel = viewModel(factory = AppViewModelFactory(container))
    val query by viewModel.query.collectAsState()
    val records by viewModel.filteredRecords.collectAsState()
    val nameSuggestions by viewModel.nameSuggestions.collectAsState()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(XLSX_MIME)) { uri ->
        uri?.let { ExcelService.exportRentalRecords(context, it, records) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importFromFile(context, it) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        AutosuggestTextField("Поиск по ФИО", query, viewModel::setQuery, nameSuggestions, Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row {
            OutlinedButton(onClick = { importLauncher.launch(arrayOf(XLSX_MIME, "text/csv", "*/*")) }) { Text("Импорт") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = { exportLauncher.launch("Информация по аренде.xlsx") }) { Text("Скачать Excel") }
        }
        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        val hScroll = rememberScrollState()
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).horizontalScroll(hScroll)) {
            HeaderRow()
            Divider()
            records.forEach { record ->
                DataRow(record, onCommit = { column, value -> viewModel.updateCell(record, column, value) })
                Divider()
            }
        }
    }
}

@Composable
private fun HeaderRow() {
    Row {
        RENTAL_INFO_COLUMNS.forEach { col ->
            Text(
                col.header,
                modifier = Modifier.width(col.widthDp.dp).padding(6.dp),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun DataRow(record: RentalRecordEntity, onCommit: (RentalColumn, String) -> Unit) {
    Row {
        RENTAL_INFO_COLUMNS.forEach { col ->
            EditableTableCell(value = col.get(record), widthDp = col.widthDp, onCommit = { newValue -> onCommit(col, newValue) })
        }
    }
}

@Composable
private fun EditableTableCell(value: String, widthDp: Int, onCommit: (String) -> Unit) {
    var text by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier
            .width(widthDp.dp)
            .padding(2.dp)
            .onFocusChanged { state -> if (!state.isFocused && text != value) onCommit(text) },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall,
    )
}
