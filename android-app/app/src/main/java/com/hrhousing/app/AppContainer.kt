package com.hrhousing.app

import android.content.Context
import com.hrhousing.app.data.db.AppDatabase
import com.hrhousing.app.data.prefs.JsonPrefStore
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

    val cityRepository = CityRepository(database.cityDao())
    val residentEmployeeRepository = ResidentEmployeeRepository(database.residentEmployeeDao())
    val landlordRepository = LandlordRepository(database.landlordDao())
    val tripEntryRepository = TripEntryRepository(database.tripEntryDao())
    val rentalRecordRepository = RentalRecordRepository(database.rentalRecordDao())
    val financeRegistryRepository = FinanceRegistryRepository(database.financeRegistryDao())

    val themePreferences = ThemePreferences(context)

    private val jsonPrefStore = JsonPrefStore(context)
    val templateStore = TemplateStore(jsonPrefStore, applicationScope)
    val sharedFieldsRepository = SharedFieldsRepository(jsonPrefStore, applicationScope)

    init {
        applicationScope.launch { cityRepository.ensureSeeded() }
    }
}
