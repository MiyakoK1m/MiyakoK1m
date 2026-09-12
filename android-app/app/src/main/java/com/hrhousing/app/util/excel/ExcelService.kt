package com.hrhousing.app.util.excel

import android.content.Context
import android.net.Uri
import com.hrhousing.app.data.db.entity.CityEntity
import com.hrhousing.app.data.db.entity.FinanceRegistryEntryEntity
import com.hrhousing.app.data.db.entity.LandlordEntity
import com.hrhousing.app.data.db.entity.RentalRecordEntity
import com.hrhousing.app.data.db.entity.ResidentEmployeeEntity
import com.hrhousing.app.data.db.entity.TripEntryEntity

data class BackupBundle(
    val tripEntries: List<TripEntryEntity> = emptyList(),
    val rentalRecords: List<RentalRecordEntity> = emptyList(),
    val landlords: List<LandlordEntity> = emptyList(),
    val residents: List<ResidentEmployeeEntity> = emptyList(),
    val cities: List<CityEntity> = emptyList(),
    val financeRegistry: List<FinanceRegistryEntryEntity> = emptyList(),
)

/** All Excel export/import/backup entry points used across the app's screens. */
object ExcelService {

    // ---------- single-section export ----------

    fun exportRentalRecords(context: Context, uri: Uri, records: List<RentalRecordEntity>) {
        val wb = openWorkbookForWrite()
        writeSheet(wb, SHEET_RENTAL_RECORDS, RENTAL_RECORD_HEADERS, records.map { it.toExcelRow() })
        wb.saveTo(context, uri)
    }

    fun exportLandlords(context: Context, uri: Uri, landlords: List<LandlordEntity>) {
        val wb = openWorkbookForWrite()
        writeSheet(wb, SHEET_LANDLORDS, LANDLORD_HEADERS, landlords.map { it.toExcelRow() })
        wb.saveTo(context, uri)
    }

    fun exportResidents(context: Context, uri: Uri, residents: List<ResidentEmployeeEntity>) {
        val wb = openWorkbookForWrite()
        writeSheet(wb, SHEET_RESIDENTS, RESIDENT_HEADERS, residents.map { it.toExcelRow() })
        wb.saveTo(context, uri)
    }

    fun exportFinanceRegistry(context: Context, uri: Uri, entries: List<FinanceRegistryEntryEntity>) {
        val wb = openWorkbookForWrite()
        writeSheet(wb, SHEET_FINANCE_REGISTRY, FINANCE_REGISTRY_HEADERS, entries.map { it.toExcelRow() })
        wb.saveTo(context, uri)
    }

    // ---------- import ----------

    /** Generic Excel/CSV import for "База данных": header matched exact-first, then partial. */
    fun importRentalRecords(context: Context, uri: Uri): List<RentalRecordEntity> {
        val rows = if (isCsv(context, uri)) {
            readCsvRows(context, uri)
        } else {
            val wb = openWorkbookFrom(context, uri) ?: return emptyList()
            wb.use { workbook -> readSheetRows(workbook.getSheetAt(0)) }
        }
        return rows.map { it.toRentalRecordEntity() }
    }

    private fun isCsv(context: Context, uri: Uri): Boolean {
        val mime = context.contentResolver.getType(uri) ?: ""
        if (mime.contains("csv")) return true
        val path = uri.lastPathSegment ?: ""
        return path.endsWith(".csv", ignoreCase = true)
    }

    /** Import button dedicated to the external corporate trip-tracking system's export format. */
    fun importTripEntriesCorporateFormat(context: Context, uri: Uri): List<TripEntryEntity> {
        val wb = openWorkbookFrom(context, uri) ?: return emptyList()
        wb.use { workbook ->
            val sheet = workbook.getSheetAt(0)
            return readSheetRows(sheet).map { it.toTripEntryEntityFromCorporateFormat() }
        }
    }

    // ---------- full backup / restore ----------

    fun exportFullBackup(context: Context, uri: Uri, bundle: BackupBundle) {
        val wb = openWorkbookForWrite()
        writeSheet(wb, SHEET_TRIP_ENTRIES, TRIP_ENTRY_HEADERS, bundle.tripEntries.map { it.toExcelRow() })
        writeSheet(wb, SHEET_RENTAL_RECORDS, RENTAL_RECORD_HEADERS, bundle.rentalRecords.map { it.toExcelRow() })
        writeSheet(wb, SHEET_LANDLORDS, LANDLORD_HEADERS, bundle.landlords.map { it.toExcelRow() })
        writeSheet(wb, SHEET_RESIDENTS, RESIDENT_HEADERS, bundle.residents.map { it.toExcelRow() })
        writeSheet(wb, SHEET_CITIES, CITY_HEADERS, bundle.cities.map { it.toExcelRow() })
        writeSheet(wb, SHEET_FINANCE_REGISTRY, FINANCE_REGISTRY_HEADERS, bundle.financeRegistry.map { it.toExcelRow() })
        wb.saveTo(context, uri)
    }

    fun importFullBackup(context: Context, uri: Uri): BackupBundle? {
        val wb = openWorkbookFrom(context, uri) ?: return null
        wb.use { workbook ->
            fun sheetRows(name: String) = workbook.getSheet(name)?.let { readSheetRows(it) } ?: emptyList()
            return BackupBundle(
                tripEntries = sheetRows(SHEET_TRIP_ENTRIES).map { it.toTripEntryEntity() },
                rentalRecords = sheetRows(SHEET_RENTAL_RECORDS).map { it.toRentalRecordEntity() },
                landlords = sheetRows(SHEET_LANDLORDS).map { it.toLandlordEntity() },
                residents = sheetRows(SHEET_RESIDENTS).map { it.toResidentEntity() },
                cities = sheetRows(SHEET_CITIES).map { it.toCityEntity() },
                financeRegistry = emptyList(), // registry rows carry a derived "Итого"; re-derive rather than reparse on restore.
            )
        }
    }

    /** Content-based de-dup: drop incoming rows that already exist (ignoring auto-generated id). */
    fun <T> mergeNew(existing: List<T>, incoming: List<T>, withoutId: (T) -> T): List<T> {
        val existingSignatures = existing.map { withoutId(it) }.toHashSet()
        return incoming.filter { withoutId(it) !in existingSignatures }
    }
}
