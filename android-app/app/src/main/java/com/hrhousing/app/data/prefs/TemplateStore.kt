package com.hrhousing.app.data.prefs

import com.hrhousing.app.data.net.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object DefaultTemplates {
    const val CHECKIN = """Информация по заселению

ЖК: {жк}
Адрес: {адрес}
Подъезд: {подъезд}
Этаж: {этаж}
Квартира №: {номер_квартиры}
Код домофона: {домофон}

Заселение: {дата_заезда} с {время_заезда}
Выселение: {дата_выезда} до {время_выезда}

2ГИС: {2гис}

Wi-Fi: {wifi_имя}
Пароль: {wifi_пароль}

{правила}
{комментарий}"""

    const val RULES_BLOCK = """Правила проживания:
• Курение в квартире запрещено
• Просьба поддерживать чистоту: не оставлять после себя беспорядок, не наносить ущерб имуществу, бережно относиться к постельному белью и полотенцам
• С 23:00 соблюдать тишину, уважая соседей
• При вызове полиции из-за нарушения тишины или иных правил ответственность несёт сотрудник
• При нарушении правил проживания возвратный депозит не возвращается — ответственность также несёт сотрудник"""

    const val BOOKING = """Подтверждение бронирования

Гости: {фио_гостей}
Город: {город}
ЖК: {жк}
Адрес: {адрес}
Тип квартиры: {тип_квартиры}

Заезд: {дата_заезда} {время_заезда}
Выезд: {дата_выезда} {время_выезда}

Стоимость за сутки: {ставка}
Депозит: {депозит}
Итого: {итого}"""

    const val FINANCE_OBJECT = """Город: {город}
ФИО: {фио}
Тип квартиры: {тип_квартиры}
Количество гостей: {гостей}
Заезд: {дата_заезда}
Выезд: {дата_выезда}
Стоимость за сутки: {ставка}
Депозит: {депозит}
Итого: {итого}
Комментарий: {комментарий}"""
}

enum class TemplateKey(val prefKey: String, val serverKey: String, val default: String) {
    CHECKIN("template_checkin", "CHECKIN", DefaultTemplates.CHECKIN),
    BOOKING("template_booking", "BOOKING", DefaultTemplates.BOOKING),
    FINANCE_OBJECT("template_finance_object", "FINANCE_OBJECT", DefaultTemplates.FINANCE_OBJECT),
}

/** Holds the user-editable text templates used by the "Заселение"/"Бронирование"/"Финансы" generators. */
class TemplateStore(
    private val jsonPrefStore: JsonPrefStore,
    private val serverSettings: ServerSettings,
    private val scope: CoroutineScope,
) {
    private val flows: Map<TemplateKey, MutableStateFlow<String>> = TemplateKey.values().associateWith { key ->
        MutableStateFlow(key.default)
    }

    init {
        for (key in TemplateKey.values()) {
            scope.launch {
                jsonPrefStore.observeRaw(key.prefKey).collectLatest { stored ->
                    flows.getValue(key).value = stored ?: key.default
                }
            }
        }
        scope.launch { refresh() }
    }

    private fun base(): String? = serverSettings.baseUrl.value.ifBlank { null }

    suspend fun refresh() {
        val url = base() ?: return
        val remote = runCatching { ApiClient.getObject(url, "/api/templates") }.getOrNull() ?: return
        for (key in TemplateKey.values()) {
            val value = remote.optString(key.serverKey, "")
            if (value.isNotBlank()) {
                flows.getValue(key).value = value
                jsonPrefStore.writeRaw(key.prefKey, value)
            }
        }
    }

    fun observe(key: TemplateKey): StateFlow<String> = flows.getValue(key)

    fun current(key: TemplateKey): String = flows.getValue(key).value.ifBlank { key.default }

    fun update(scope: CoroutineScope, key: TemplateKey, value: String) {
        flows.getValue(key).value = value
        scope.launch {
            jsonPrefStore.writeRaw(key.prefKey, value)
            val url = base() ?: return@launch
            runCatching { ApiClient.putObject(url, "/api/templates/${key.serverKey}", org.json.JSONObject().put("value", value)) }
        }
    }
}
