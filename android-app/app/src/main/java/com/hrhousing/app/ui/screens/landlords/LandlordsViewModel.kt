package com.hrhousing.app.ui.screens.landlords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.db.entity.LandlordEntity
import com.hrhousing.app.data.db.entity.OwnerType
import com.hrhousing.app.data.db.entity.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LandlordFormState(
    val fullName: String = "",
    val phone: String = "",
    val ownerType: OwnerType = OwnerType.OWNER,
    val city: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.TRANSFER,
    val rating: Int = 7,
    val comment: String = "",
)

class LandlordsViewModel(private val container: AppContainer) : ViewModel() {
    val landlords: StateFlow<List<LandlordEntity>> = container.landlordRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cityOptions: StateFlow<List<String>> = container.cityRepository.observeAll()
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _form = MutableStateFlow(LandlordFormState())
    val form: StateFlow<LandlordFormState> = _form

    fun updateForm(transform: (LandlordFormState) -> LandlordFormState) { _form.value = transform(_form.value) }

    fun addLandlord() {
        val f = _form.value
        if (f.fullName.isBlank()) return
        viewModelScope.launch {
            container.landlordRepository.upsert(
                LandlordEntity(
                    fullName = f.fullName, phone = f.phone, ownerType = f.ownerType,
                    city = f.city, paymentMethod = f.paymentMethod, rating = f.rating, comment = f.comment,
                ),
            )
            _form.value = LandlordFormState()
        }
    }

    fun delete(entity: LandlordEntity) {
        viewModelScope.launch { container.landlordRepository.delete(entity) }
    }

    fun exportToExcel(context: android.content.Context, uri: android.net.Uri) {
        com.hrhousing.app.util.excel.ExcelService.exportLandlords(context, uri, landlords.value)
    }
}
