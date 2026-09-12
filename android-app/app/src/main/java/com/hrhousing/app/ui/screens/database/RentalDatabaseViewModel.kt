package com.hrhousing.app.ui.screens.database

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.db.entity.ApartmentType
import com.hrhousing.app.data.db.entity.RentalRecordEntity
import com.hrhousing.app.data.db.entity.roomsCount
import com.hrhousing.app.data.shared.checkinDate
import com.hrhousing.app.data.shared.checkoutDate
import com.hrhousing.app.util.FileUtils
import com.hrhousing.app.util.excel.BackupBundle
import com.hrhousing.app.util.excel.ExcelService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.temporal.ChronoUnit

class RentalDatabaseViewModel(private val container: AppContainer) : ViewModel() {

    private val _draft = MutableStateFlow(freshDraft())
    val draft: StateFlow<RentalRecordEntity> = _draft

    val records: StateFlow<List<RentalRecordEntity>> = container.rentalRecordRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAll = MutableStateFlow(false)
    val showAll: StateFlow<Boolean> = _showAll
    fun toggleShowAll() { _showAll.value = !_showAll.value }

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    private fun freshDraft(): RentalRecordEntity {
        val shared = container.sharedFieldsRepository.fields.value
        return RentalRecordEntity(
            city = shared.city,
            address = shared.address,
            apartmentType = shared.apartmentType,
            apartmentTypeOtherText = shared.apartmentTypeOtherText,
            complexName = shared.complexName,
            fullName = shared.guestNames.firstOrNull { it.isNotBlank() } ?: "",
            rentStartDate = shared.checkinDate(),
            rentEndDate = shared.checkoutDate(),
            roomsCount = shared.apartmentType.roomsCount(),
            checkinTimeFrom = shared.checkinTime,
            checkoutTimeTo = shared.checkoutTime,
        )
    }

    fun updateDraft(transform: (RentalRecordEntity) -> RentalRecordEntity) {
        _draft.value = transform(_draft.value)
    }

    /** Apartment type is one of the explicitly cross-synced fields, so pushing it here also updates it everywhere else. */
    fun setApartmentType(type: ApartmentType, other: String) {
        updateDraft { it.copy(apartmentType = type, apartmentTypeOtherText = other, roomsCount = type.roomsCount()) }
        container.sharedFieldsRepository.setApartmentType(type, other)
    }

    fun recomputeDerived() {
        val d = _draft.value
        val start = d.rentStartDate
        val end = d.rentEndDate
        val days = if (start != null && end != null) ChronoUnit.DAYS.between(start, end).toInt().coerceAtLeast(0) else d.rentDurationDays
        _draft.value = d.copy(rentDurationDays = days, rentTotalAmount = d.rentAmount * days)
    }

    fun saveDraft() {
        viewModelScope.launch {
            recomputeDerived()
            container.rentalRecordRepository.upsert(_draft.value.copy(id = 0))
            _draft.value = freshDraft()
            _statusMessage.value = "Запись сохранена"
        }
    }

    fun deleteRecord(record: RentalRecordEntity) {
        viewModelScope.launch { container.rentalRecordRepository.delete(record) }
    }

    fun attachReceipt(context: Context, sourceUri: Uri, description: String, destUri: Uri) {
        val originalName = FileUtils.displayName(context, sourceUri)
        val ext = FileUtils.extensionOf(originalName)
        FileUtils.copy(context, sourceUri, destUri)
        val fileName = buildReceiptFileName(description, ext)
        updateDraft { it.copy(receiptFileName = fileName, receiptUri = destUri.toString(), receiptDescription = description) }
    }

    fun buildReceiptFileName(description: String, extension: String): String {
        val d = _draft.value
        val date = d.rentStartDate?.toString() ?: "без_даты"
        val city = FileUtils.sanitizeForFileName(d.city.ifBlank { "город" })
        val name = FileUtils.sanitizeForFileName(d.fullName.ifBlank { "ФИО" })
        val desc = FileUtils.sanitizeForFileName(description)
        val suffix = if (extension.isNotBlank()) ".$extension" else ""
        return "${date}_${city}_${name}_$desc$suffix"
    }

    fun deleteAllData() {
        viewModelScope.launch {
            container.tripEntryRepository.clear()
            container.rentalRecordRepository.clear()
            container.financeRegistryRepository.clear()
            container.landlordRepository.clear()
            container.residentEmployeeRepository.clear()
            container.cityRepository.resetToDefault()
            _draft.value = freshDraft()
            _statusMessage.value = "Все данные удалены"
        }
    }

    fun importFromFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            val imported = ExcelService.importRentalRecords(context, uri)
            container.rentalRecordRepository.insertAll(imported)
            _statusMessage.value = "Импортировано записей: ${imported.size}"
        }
    }

    fun importCorporateFormat(context: Context, uri: Uri) {
        viewModelScope.launch {
            val imported = ExcelService.importTripEntriesCorporateFormat(context, uri)
            container.tripEntryRepository.insertAll(imported)
            _statusMessage.value = "Импортировано командировок: ${imported.size}"
        }
    }

    fun exportToExcel(context: Context, uri: Uri) {
        ExcelService.exportRentalRecords(context, uri, records.value)
        _statusMessage.value = "Экспортировано"
    }

    fun exportFullBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            val fullBundle = BackupBundle(
                tripEntries = container.tripEntryRepository.getAll(),
                rentalRecords = container.rentalRecordRepository.getAll(),
                landlords = container.landlordRepository.observeAll().first(),
                residents = container.residentEmployeeRepository.observeAll().first(),
                cities = container.cityRepository.observeAll().first(),
                financeRegistry = container.financeRegistryRepository.observeAll().first(),
            )
            ExcelService.exportFullBackup(context, uri, fullBundle)
            _statusMessage.value = "Резервная копия сохранена"
        }
    }

    fun restoreFromBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            val bundle = ExcelService.importFullBackup(context, uri) ?: return@launch
            val existingTrips = container.tripEntryRepository.getAll()
            val existingRentals = container.rentalRecordRepository.getAll()
            val existingLandlords = container.landlordRepository.observeAll().first()
            val existingResidents = container.residentEmployeeRepository.observeAll().first()
            val existingCities = container.cityRepository.observeAll().first()

            container.tripEntryRepository.insertAll(
                ExcelService.mergeNew(existingTrips, bundle.tripEntries) { it.copy(id = 0, createdAt = 0) },
            )
            container.rentalRecordRepository.insertAll(
                ExcelService.mergeNew(existingRentals, bundle.rentalRecords) { it.copy(id = 0, createdAt = 0) },
            )
            container.landlordRepository.insertAll(
                ExcelService.mergeNew(existingLandlords, bundle.landlords) { it.copy(id = 0) },
            )
            container.residentEmployeeRepository.insertAll(
                ExcelService.mergeNew(existingResidents, bundle.residents) { it.copy(id = 0) },
            )
            val existingCityNames = existingCities.map { it.name }.toHashSet()
            bundle.cities.filter { it.name !in existingCityNames }.forEach { container.cityRepository.add(it.name) }

            _statusMessage.value = "Восстановление из резервной копии завершено"
        }
    }
}
