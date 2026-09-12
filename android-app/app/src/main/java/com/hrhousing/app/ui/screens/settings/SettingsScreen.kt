package com.hrhousing.app.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrhousing.app.AppContainer
import com.hrhousing.app.ui.AppViewModelFactory

@Composable
fun SettingsScreen(container: AppContainer) {
    val viewModel: SettingsViewModel = viewModel(factory = AppViewModelFactory(container))
    val savedUrl by viewModel.baseUrl.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    var urlText by remember(savedUrl) { mutableStateOf(savedUrl) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Синхронизация с сайтом", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Укажите адрес развёрнутого веб-компаньона (см. web/README.md), чтобы это приложение " +
                "читало и писало в ту же базу, что и сайт. Оставьте поле пустым, чтобы работать " +
                "полностью локально/офлайн, как раньше.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = urlText,
            onValueChange = { urlText = it },
            label = { Text("Адрес сервера") },
            placeholder = { Text("https://hr-housing-web.onrender.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = { viewModel.setUrl(urlText); viewModel.checkConnection() }, modifier = Modifier.fillMaxWidth()) {
            Text("Сохранить адрес")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { viewModel.checkConnection() }, modifier = Modifier.fillMaxWidth()) {
            Text("Проверить соединение")
        }
        Spacer(Modifier.height(8.dp))
        val stateText = when (connectionState) {
            ConnectionState.UNKNOWN -> "Соединение не проверялось"
            ConnectionState.CHECKING -> "Проверка…"
            ConnectionState.ONLINE -> "✅ Сервер доступен"
            ConnectionState.OFFLINE -> "⚠ Сервер недоступен — приложение продолжит работать локально"
        }
        Text(stateText, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(20.dp))
        Button(onClick = { viewModel.syncNow() }, modifier = Modifier.fillMaxWidth()) {
            Text("Синхронизировать сейчас")
        }
        statusMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
