package com.hrhousing.app.ui.screens.residents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.db.entity.ResidentEmployeeEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ResidentFormState(val fullName: String = "", val comment: String = "")

class ResidentsViewModel(private val container: AppContainer) : ViewModel() {
    val residents: StateFlow<List<ResidentEmployeeEntity>> = container.residentEmployeeRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _form = MutableStateFlow(ResidentFormState())
    val form: StateFlow<ResidentFormState> = _form

    fun updateForm(transform: (ResidentFormState) -> ResidentFormState) { _form.value = transform(_form.value) }

    fun add() {
        val f = _form.value
        if (f.fullName.isBlank()) return
        viewModelScope.launch {
            container.residentEmployeeRepository.upsert(ResidentEmployeeEntity(fullName = f.fullName, comment = f.comment))
            _form.value = ResidentFormState()
        }
    }

    fun delete(entity: ResidentEmployeeEntity) {
        viewModelScope.launch { container.residentEmployeeRepository.delete(entity) }
    }

    fun exportToExcel(context: android.content.Context, uri: android.net.Uri) {
        com.hrhousing.app.util.excel.ExcelService.exportResidents(context, uri, residents.value)
    }
}
