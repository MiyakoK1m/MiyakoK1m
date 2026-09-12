package com.hrhousing.app.ui.screens.residents

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.db.entity.ResidentEmployeeEntity
import com.hrhousing.app.ui.AppViewModelFactory

private const val XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

@Composable
fun ResidentsScreen(container: AppContainer) {
    val viewModel: ResidentsViewModel = viewModel(factory = AppViewModelFactory(container))
    val residents by viewModel.residents.collectAsState()
    val form by viewModel.form.collectAsState()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(XLSX_MIME)) { uri ->
        uri?.let { viewModel.exportToExcel(context, it) }
    }

    LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Новый сотрудник", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(form.fullName, { v -> viewModel.updateForm { it.copy(fullName = v) } }, label = { Text("ФИО сотрудника") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                form.comment,
                { v -> viewModel.updateForm { it.copy(comment = v) } },
                label = { Text("Комментарий") },
                placeholder = { Text("Например: Проживает в Алматы до 20.08.2026") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = viewModel::add, modifier = Modifier.fillMaxWidth()) { Text("Добавить") }
            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(12.dp))
            Text("Проживающие сотрудники (${residents.size})", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }

        items(residents, key = { it.id }) { resident ->
            ResidentRow(resident, onDelete = { viewModel.delete(resident) })
            Spacer(Modifier.height(8.dp))
        }

        item {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { exportLauncher.launch("Проживающие сотрудники.xlsx") }, modifier = Modifier.fillMaxWidth()) {
                Text("Скачать Excel")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ResidentRow(resident: ResidentEmployeeEntity, onDelete: () -> Unit) {
    Card {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(resident.fullName, style = MaterialTheme.typography.bodyLarge)
                if (resident.comment.isNotBlank()) Text(resident.comment, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Close, contentDescription = "Удалить") }
        }
    }
}
