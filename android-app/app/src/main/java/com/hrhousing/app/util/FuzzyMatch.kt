package com.hrhousing.app.util

/** Case/order-insensitive full-name matching ("Иванов Иван" == "Иван Иванов" == "иванов иван"). */
object FuzzyMatch {
    private fun normalizedWords(name: String): Set<String> =
        name.trim().lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }.toSet()

    fun sameName(a: String, b: String): Boolean {
        val wa = normalizedWords(a)
        val wb = normalizedWords(b)
        if (wa.isEmpty() || wb.isEmpty()) return false
        return wa == wb
    }
}
