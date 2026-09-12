package com.hrhousing.app.util.excel

import android.content.Context
import android.net.Uri

/** Minimal RFC4180-ish CSV reader so "Импорт из Excel/CSV" also accepts plain .csv exports. */
fun readCsvRows(context: Context, uri: Uri): List<ImportRow> {
    val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: return emptyList()
    val lines = text.lines().filter { it.isNotBlank() }
    if (lines.isEmpty()) return emptyList()
    val headers = parseCsvLine(lines.first())
    return lines.drop(1).map { line ->
        val cells = parseCsvLine(line)
        ImportRow(headers, cells)
    }
}

private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val c = line[i]
        when {
            inQuotes && c == '"' && i + 1 < line.length && line[i + 1] == '"' -> {
                current.append('"'); i++
            }
            c == '"' -> inQuotes = !inQuotes
            (c == ',' || c == ';') && !inQuotes -> {
                result.add(current.toString()); current.clear()
            }
            else -> current.append(c)
        }
        i++
    }
    result.add(current.toString())
    return result.map { it.trim() }
}
