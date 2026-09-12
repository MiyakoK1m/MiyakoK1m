package com.hrhousing.app.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * All "today" and date-math in this app must use Asia/Almaty (UTC+5), never the device's own
 * time zone, per spec: HR entries/dashboards are meaningless if they drift with the phone's TZ.
 */
object TimeUtils {
    val ALMATY_ZONE: ZoneId = ZoneId.of("Asia/Almaty")

    fun today(): LocalDate = LocalDate.now(ALMATY_ZONE)

    fun now(): LocalDateTime = LocalDateTime.now(ALMATY_ZONE)

    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun format(date: LocalDate?): String = date?.format(dateFormatter) ?: ""

    fun parseOrNull(text: String): LocalDate? =
        try {
            LocalDate.parse(text.trim(), dateFormatter)
        } catch (e: Exception) {
            null
        }

    /** True when [today] falls within [start]..[end] inclusive. */
    fun isActiveToday(start: LocalDate?, end: LocalDate?, today: LocalDate = today()): Boolean {
        if (start == null || end == null) return false
        return !today.isBefore(start) && !today.isAfter(end)
    }

    fun rangesOverlap(aStart: LocalDate?, aEnd: LocalDate?, bStart: LocalDate?, bEnd: LocalDate?): Boolean {
        if (aStart == null || aEnd == null || bStart == null || bEnd == null) return false
        return !aStart.isAfter(bEnd) && !bStart.isAfter(aEnd)
    }
}

fun Long.formatMoney(): String {
    val text = kotlin.math.abs(this).toString()
    val grouped = StringBuilder()
    for ((index, ch) in text.reversed().withIndex()) {
        if (index != 0 && index % 3 == 0) grouped.append(' ')
        grouped.append(ch)
    }
    val sign = if (this < 0) "-" else ""
    return "$sign${grouped.reverse()} ₸"
}
