package com.hrhousing.app.data.prefs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

enum class TemplateKey(val prefKey: String, val default: String) {
    CHECKIN("template_checkin", DefaultTemplates.CHECKIN),
    BOOKING("template_booking", DefaultTemplates.BOOKING),
    FINANCE_OBJECT("template_finance_object", DefaultTemplates.FINANCE_OBJECT),
}

/** Holds the user-editable text templates used by the "Заселение"/"Бронирование"/"Финансы" generators. */
class TemplateStore(private val jsonPrefStore: JsonPrefStore, scope: CoroutineScope) {
    private val flows: Map<TemplateKey, StateFlow<String>> = TemplateKey.values().associateWith { key ->
        jsonPrefStore.observeRaw(key.prefKey)
            .map { it ?: key.default }
            .stateIn(scope, SharingStarted.Eagerly, key.default)
    }

    fun observe(key: TemplateKey): StateFlow<String> = flows.getValue(key)

    fun current(key: TemplateKey): String = flows.getValue(key).value.ifBlank { key.default }

    fun update(scope: CoroutineScope, key: TemplateKey, value: String) {
        scope.launch { jsonPrefStore.writeRaw(key.prefKey, value) }
    }
}
