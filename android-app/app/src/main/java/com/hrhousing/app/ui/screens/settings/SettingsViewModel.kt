package com.hrhousing.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.net.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ConnectionState { UNKNOWN, CHECKING, ONLINE, OFFLINE }

class SettingsViewModel(private val container: AppContainer) : ViewModel() {
    val baseUrl: StateFlow<String> = container.serverSettings.baseUrl

    private val _connectionState = MutableStateFlow(ConnectionState.UNKNOWN)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    fun setUrl(url: String) {
        container.serverSettings.update(viewModelScope, url)
        _connectionState.value = ConnectionState.UNKNOWN
    }

    fun checkConnection() {
        val url = baseUrl.value
        if (url.isBlank()) { _connectionState.value = ConnectionState.UNKNOWN; return }
        _connectionState.value = ConnectionState.CHECKING
        viewModelScope.launch {
            val ok = ApiClient.checkHealth(url)
            _connectionState.value = if (ok) ConnectionState.ONLINE else ConnectionState.OFFLINE
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _statusMessage.value = "Синхронизация…"
            runCatching { container.refreshAllFromServer() }
                .onSuccess { _statusMessage.value = "Синхронизировано" }
                .onFailure { _statusMessage.value = "Не удалось синхронизировать: ${it.message}" }
            checkConnection()
        }
    }
}
