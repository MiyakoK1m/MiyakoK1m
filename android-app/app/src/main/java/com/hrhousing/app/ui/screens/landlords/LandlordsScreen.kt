package com.hrhousing.app.ui.screens.landlords

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
import com.hrhousing.app.data.db.entity.LandlordEntity
import com.hrhousing.app.data.db.entity.OwnerType
import com.hrhousing.app.data.db.entity.PaymentMethod
import com.hrhousing.app.ui.AppViewModelFactory
import com.hrhousing.app.ui.components.AutosuggestTextField
import com.hrhousing.app.ui.components.TilePicker

private const val XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

@Composable
fun LandlordsScreen(container: AppContainer) {
    val viewModel: LandlordsViewModel = viewModel(factory = AppViewModelFactory(container))
    val landlords by viewModel.landlords.collectAsState()
    val form by viewModel.form.collectAsState()
    val cityOptions by viewModel.cityOptions.collectAsState()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(XLSX_MIME)) { uri ->
        uri?.let { viewModel.exportToExcel(context, it) }
    }

    LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Новый арендодатель", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(form.fullName, { v -> viewModel.updateForm { it.copy(fullName = v) } }, label = { Text("ФИО арендодателя") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(form.phone, { v -> viewModel.updateForm { it.copy(phone = v) } }, label = { Text("Контактный номер") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            TilePicker(listOf(OwnerType.OWNER, OwnerType.AGENT), form.ownerType, { v -> viewModel.updateForm { it.copy(ownerType = v) } }, {
                if (it == OwnerType.OWNER) "Собственник" else "Риелтор"
            })
            Spacer(Modifier.height(8.dp))
            AutosuggestTextField("Город", form.city, { v -> viewModel.updateForm { it.copy(city = v) } }, cityOptions, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            TilePicker(listOf(PaymentMethod.INVOICE, PaymentMethod.TRANSFER), form.paymentMethod, { v -> viewModel.updateForm { it.copy(paymentMethod = v) } }, {
                if (it == PaymentMethod.INVOICE) "Счёт" else "Перевод"
            })
            Spacer(Modifier.height(8.dp))
            Text("Рейтинг", style = MaterialTheme.typography.titleSmall)
            TilePicker((1..10).toList(), form.rating, { v -> viewModel.updateForm { it.copy(rating = v) } }, { it.toString() })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(form.comment, { v -> viewModel.updateForm { it.copy(comment = v) } }, label = { Text("Комментарий") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button(onClick = viewModel::addLandlord, modifier = Modifier.fillMaxWidth()) { Text("Добавить") }
            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(12.dp))
            Text("Арендодатели (${landlords.size})", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }

        items(landlords, key = { it.id }) { landlord ->
            LandlordRow(landlord, onDelete = { viewModel.delete(landlord) })
            Spacer(Modifier.height(8.dp))
        }

        item {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { exportLauncher.launch("Арендодатели.xlsx") }, modifier = Modifier.fillMaxWidth()) {
                Text("Скачать Excel")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LandlordRow(landlord: LandlordEntity, onDelete: () -> Unit) {
    Card {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(landlord.fullName, style = MaterialTheme.typography.bodyLarge)
                Text("${landlord.phone} · ${landlord.city} · ★${landlord.rating}", style = MaterialTheme.typography.bodySmall)
                if (landlord.comment.isNotBlank()) Text(landlord.comment, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Close, contentDescription = "Удалить") }
        }
    }
}
