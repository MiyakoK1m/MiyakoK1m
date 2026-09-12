package com.hrhousing.app.ui.screens.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrhousing.app.AppContainer
import com.hrhousing.app.data.prefs.DefaultTemplates
import com.hrhousing.app.data.prefs.TemplateKey
import com.hrhousing.app.data.shared.SharedFields
import com.hrhousing.app.data.shared.checkinDate
import com.hrhousing.app.data.shared.checkoutDate
import com.hrhousing.app.util.TemplateEngine
import com.hrhousing.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CheckinLocalState(
    val entrance: String = "",
    val entranceNote: String = "",
    val floor: String = "",
    val apartmentNumber: String = "",
    val intercomCode: String = "",
    val twoGisLinkText: String = "",
    val twoGisLinkUrl: String = "",
    val wifiName: String = "",
    val wifiPassword: String = "",
    val includeRulesBlock: Boolean = true,
    val additionalComment: String = "",
)

class CheckinInfoViewModel(private val container: AppContainer) : ViewModel() {
    private val _local = MutableStateFlow(CheckinLocalState())
    val local: StateFlow<CheckinLocalState> = _local

    val sharedFields: StateFlow<SharedFields> = container.sharedFieldsRepository.fields
    val template: StateFlow<String> = container.templateStore.observe(TemplateKey.CHECKIN)

    val generatedText: StateFlow<String> = combine(sharedFields, _local, template) { shared, local, tmpl ->
        val tokens = mapOf(
            "жк" to shared.complexName,
            "адрес" to shared.address,
            "подъезд" to listOf(local.entrance, local.entranceNote).filter { it.isNotBlank() }.joinToString(" "),
            "этаж" to local.floor,
            "номер_квартиры" to local.apartmentNumber,
            "домофон" to local.intercomCode,
            "дата_заезда" to TimeUtils.format(shared.checkinDate()),
            "время_заезда" to shared.checkinTime,
            "дата_выезда" to TimeUtils.format(shared.checkoutDate()),
            "время_выезда" to shared.checkoutTime,
            "2гис" to if (local.twoGisLinkText.isNotBlank() && local.twoGisLinkUrl.isNotBlank()) {
                "[${local.twoGisLinkText}](${local.twoGisLinkUrl})"
            } else "",
            "wifi_имя" to local.wifiName,
            "wifi_пароль" to local.wifiPassword,
            "правила" to if (local.includeRulesBlock) DefaultTemplates.RULES_BLOCK else "",
            "комментарий" to local.additionalComment,
        )
        TemplateEngine.render(tmpl, tokens)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun updateLocal(transform: (CheckinLocalState) -> CheckinLocalState) {
        _local.value = transform(_local.value)
    }

    fun updateTemplate(text: String) = container.templateStore.update(viewModelScope, TemplateKey.CHECKIN, text)

    fun setComplexName(v: String) = container.sharedFieldsRepository.setComplexName(v)
    fun setAddress(v: String) = container.sharedFieldsRepository.setAddress(v)
    fun setCheckinTime(v: String) = container.sharedFieldsRepository.setCheckinTime(v)
    fun setCheckoutTime(v: String) = container.sharedFieldsRepository.setCheckoutTime(v)
}
