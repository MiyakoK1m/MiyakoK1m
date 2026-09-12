package com.hrhousing.app

import android.content.Context
import com.hrhousing.app.data.db.AppDatabase
import com.hrhousing.app.data.prefs.JsonPrefStore
import com.hrhousing.app.data.prefs.ServerSettings
import com.hrhousing.app.data.prefs.TemplateStore
import com.hrhousing.app.data.prefs.ThemePreferences
import com.hrhousing.app.data.repo.CityRepository
import com.hrhousing.app.data.repo.FinanceRegistryRepository
import com.hrhousing.app.data.repo.LandlordRepository
import com.hrhousing.app.data.repo.RentalRecordRepository
import com.hrhousing.app.data.repo.ResidentEmployeeRepository
import com.hrhousing.app.data.repo.TripEntryRepository
import com.hrhousing.app.data.shared.SharedFieldsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Simple hand-rolled service locator (no DI framework needed for a single-user, single-module app). */
class AppContainer(context: Context) {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val database = AppDatabase.getInstance(context)

    val themePreferences = ThemePreferences(context)

    private val jsonPrefStore = JsonPrefStore(context)
    val serverSettings = ServerSettings(jsonPrefStore, applicationScope)

    val cityRepository = CityRepository(database.cityDao(), serverSettings)
    val residentEmployeeRepository = ResidentEmployeeRepository(database.residentEmployeeDao(), serverSettings)
    val landlordRepository = LandlordRepository(database.landlordDao(), serverSettings)
    val tripEntryRepository = TripEntryRepository(database.tripEntryDao(), serverSettings)
    val rentalRecordRepository = RentalRecordRepository(database.rentalRecordDao(), serverSettings)
    val financeRegistryRepository = FinanceRegistryRepository(database.financeRegistryDao(), serverSettings)

    val templateStore = TemplateStore(jsonPrefStore, serverSettings, applicationScope)
    val sharedFieldsRepository = SharedFieldsRepository(jsonPrefStore, serverSettings, applicationScope)

    init {
        applicationScope.launch { cityRepository.ensureSeeded() }
        // Whenever a server is (or becomes) configured, pull its data into the local cache so
        // screens show it without the user having to visit Settings and tap "Синхронизировать".
        applicationScope.launch {
            serverSettings.baseUrl.collect { url ->
                if (url.isNotBlank()) runCatching { refreshAllFromServer() }
            }
        }
    }

    /** Pulls every table fresh from the configured server; called by the Settings screen's "Синхронизировать" button. */
    suspend fun refreshAllFromServer() {
        cityRepository.refresh()
        residentEmployeeRepository.refresh()
        landlordRepository.refresh()
        tripEntryRepository.refresh()
        rentalRecordRepository.refresh()
        financeRegistryRepository.refresh()
        templateStore.refresh()
        sharedFieldsRepository.refresh()
    }

    /**
     * "Удалить все данные": when a server is configured this must wipe the server's copy too —
     * otherwise the next refresh (or the next screen's own reload) would silently pull everything
     * back from the server, making the button a no-op. Falls back to a purely local wipe when
     * there's no server, or it can't be reached.
     */
    suspend fun deleteAllData() {
        val url = serverSettings.baseUrl.value.ifBlank { null }
        if (url != null) {
            val wiped = runCatching { com.hrhousing.app.data.net.ApiClient.delete(url, "/api/all-data") }.isSuccess
            if (wiped) {
                refreshAllFromServer()
                return
            }
        }
        tripEntryRepository.clear()
        rentalRecordRepository.clear()
        financeRegistryRepository.clear()
        landlordRepository.clear()
        residentEmployeeRepository.clear()
        cityRepository.resetToDefault()
    }
}
