package com.hrhousing.app.ui.screens.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.db.entity.CityEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CitiesViewModel(private val container: AppContainer) : ViewModel() {
    val cities: StateFlow<List<CityEntity>> = container.cityRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _newCityText = MutableStateFlow("")
    val newCityText: StateFlow<String> = _newCityText
    fun setNewCityText(text: String) { _newCityText.value = text }

    fun addCity() {
        val name = _newCityText.value.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            container.cityRepository.add(name)
            _newCityText.value = ""
        }
    }

    fun delete(city: CityEntity) {
        viewModelScope.launch { container.cityRepository.delete(city) }
    }

    fun resetToDefault() {
        viewModelScope.launch { container.cityRepository.resetToDefault() }
    }
}
