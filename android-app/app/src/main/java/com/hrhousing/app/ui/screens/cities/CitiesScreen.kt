package com.hrhousing.app.ui.screens.cities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrhousing.app.AppContainer
import com.hrhousing.app.ui.AppViewModelFactory

@Composable
fun CitiesScreen(container: AppContainer) {
    val viewModel: CitiesViewModel = viewModel(factory = AppViewModelFactory(container))
    val cities by viewModel.cities.collectAsState()
    val newCityText by viewModel.newCityText.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Города (${cities.size})", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newCityText,
                    onValueChange = viewModel::setNewCityText,
                    label = { Text("Новый город") },
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = viewModel::addCity) { Text("Добавить") }
            }
            Spacer(Modifier.height(16.dp))
        }

        items(cities, key = { it.id }) { city ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(city.name, style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = { viewModel.delete(city) }) { Icon(Icons.Filled.Close, contentDescription = "Удалить") }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = viewModel::resetToDefault, modifier = Modifier.fillMaxWidth()) {
                Text("Восстановить список по умолчанию")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
