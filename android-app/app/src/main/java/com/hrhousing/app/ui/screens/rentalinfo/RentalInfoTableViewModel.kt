package com.hrhousing.app.ui.screens.rentalinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.db.entity.RentalRecordEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RentalInfoTableViewModel(private val container: AppContainer) : ViewModel() {
    private val allRecords: StateFlow<List<RentalRecordEntity>> = container.rentalRecordRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query
    fun setQuery(text: String) { _query.value = text }

    val filteredRecords: StateFlow<List<RentalRecordEntity>> = combine(allRecords, _query) { records, q ->
        if (q.isBlank()) records else records.filter { it.fullName.contains(q, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nameSuggestions: StateFlow<List<String>> = allRecords
        .map { records -> records.map { it.fullName }.filter { it.isNotBlank() }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateCell(record: RentalRecordEntity, column: RentalColumn, newValue: String) {
        val updated = column.set(record, newValue)
        viewModelScope.launch { container.rentalRecordRepository.update(updated) }
    }

    fun importFromFile(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            val imported = com.hrhousing.app.util.excel.ExcelService.importRentalRecords(context, uri)
            container.rentalRecordRepository.insertAll(imported)
        }
    }
}
