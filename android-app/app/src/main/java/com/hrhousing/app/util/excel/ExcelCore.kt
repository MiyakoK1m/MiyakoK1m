package com.hrhousing.app.util.excel

import android.content.Context
import android.net.Uri
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

/** Column header matching: exact text match first, then a loose substring match either way. */
fun matchHeaderIndex(headers: List<String>, target: String): Int? {
    val normTarget = target.trim().lowercase()
    headers.forEachIndexed { i, h -> if (h.trim().lowercase() == normTarget) return i }
    headers.forEachIndexed { i, h ->
        val nh = h.trim().lowercase()
        if (nh.isNotBlank() && (nh.contains(normTarget) || normTarget.contains(nh))) return i
    }
    return null
}

/** One parsed data row, exposing header-aware lookups so importers can tolerate renamed columns. */
class ImportRow(private val headers: List<String>, private val cells: List<String>) {
    fun get(vararg candidates: String): String {
        for (c in candidates) {
            val idx = matchHeaderIndex(headers, c)
            if (idx != null && idx < cells.size) return cells[idx]
        }
        return ""
    }
}

private val formatter = DataFormatter()

fun readSheetRows(sheet: Sheet): List<ImportRow> {
    val rowIterator = sheet.rowIterator()
    if (!rowIterator.hasNext()) return emptyList()
    val headerRow = rowIterator.next()
    val headers = (0 until headerRow.lastCellNum.coerceAtLeast(0)).map { i ->
        formatter.formatCellValue(headerRow.getCell(i)) ?: ""
    }
    val rows = mutableListOf<ImportRow>()
    while (rowIterator.hasNext()) {
        val row = rowIterator.next()
        val cells = (0 until headers.size).map { i -> formatter.formatCellValue(row.getCell(i)) ?: "" }
        if (cells.any { it.isNotBlank() }) rows.add(ImportRow(headers, cells))
    }
    return rows
}

/** A single exportable cell: either free text or a real numeric value (so totals can be summed in Excel). */
sealed class Cv {
    data class Text(val value: String) : Cv()
    data class Num(val value: Double) : Cv()

    companion object {
        fun of(value: String) = Text(value)
        fun of(value: Long) = Num(value.toDouble())
        fun of(value: Int) = Num(value.toDouble())
    }
}

private fun Row.write(index: Int, cv: Cv) {
    when (cv) {
        is Cv.Text -> createCell(index, CellType.STRING).setCellValue(cv.value)
        is Cv.Num -> createCell(index, CellType.NUMERIC).setCellValue(cv.value)
    }
}

fun writeSheet(workbook: Workbook, sheetName: String, headers: List<String>, rows: List<List<Cv>>) {
    val sheet = workbook.createSheet(sheetName)
    val headerRow = sheet.createRow(0)
    headers.forEachIndexed { i, h -> headerRow.write(i, Cv.Text(h)) }
    rows.forEachIndexed { rIdx, rowCells ->
        val row = sheet.createRow(rIdx + 1)
        rowCells.forEachIndexed { cIdx, cv -> row.write(cIdx, cv) }
    }
}

fun openWorkbookForWrite(): Workbook = XSSFWorkbook()

fun Workbook.saveTo(context: Context, uri: Uri) {
    context.contentResolver.openOutputStream(uri, "wt")?.use { out -> write(out) }
    close()
}

fun openWorkbookFrom(context: Context, uri: Uri): Workbook? =
    context.contentResolver.openInputStream(uri)?.use { XSSFWorkbook(it) }
