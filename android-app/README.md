# Заселение и аренда — Android app

Native Android app (Kotlin + Jetpack Compose, Material 3) for an HR team that arranges short-term
housing rentals for employees on business trips in Kazakhstan. Single user, works offline, all
data stored locally on-device with Room (SQLite). Built from the spec in
`prompt_android_app.md` (11 screens, cross-field sync, generated message templates, Excel
import/export/backup).

## ⚠️ Not build-verified in this environment

This project was authored in a sandbox that has **no Android SDK, no emulator, and no network
access to Google's/Maven's dependency servers**, so it could not be opened in Android Studio,
Gradle-synced, compiled, or run here. Every file was written carefully and re-read for API
correctness, but there has been **no compiler feedback loop** — treat the first build in Android
Studio as the real first test pass. Most likely friction points on a first sync:

- Exact dependency versions in `gradle/libs.versions.toml` may need a bump if a listed version has
  since been yanked (Compose BOM, AGP, Room, KSP, Apache POI).
- Apache POI (`poi` + `poi-ooxml`) is heavy for Android — pulls in `xmlbeans`/`curvesapi`/etc. The
  `packaging { resources { excludes += ... } }` block in `app/build.gradle.kts` and the ProGuard
  keep-rules in `app/proguard-rules.pro` cover the usual conflicts (duplicate `META-INF` license
  files, R8 stripping POI's reflective schema classes), but a real build may surface one more edge
  case to exclude.
- Compose's experimental API surface (`ExposedDropdownMenuBox`, `FlowRow`) shifts version to
  version; `@OptIn` annotations are already placed everywhere they're used, but if the BOM version
  changes, the marker annotation names might too.

## Requirements

- Android Studio (Ladybug or newer) with JDK 17
- Android SDK: compileSdk 34, minSdk 26 (Android 8.0+)

## Getting started

Open the `android-app/` folder as an Android Studio project (not the repo root — this app lives
alongside an unrelated existing project in the repo). Let Gradle sync, then run the `app`
configuration on a device/emulator.

## Architecture

- **No DI framework.** `AppContainer` (`app/.../AppContainer.kt`) is a hand-rolled service locator
  built once in `HrHousingApp.onCreate`; screen ViewModels take a single `AppContainer`
  constructor argument, wired up via `AppViewModelFactory`.
- **Room** (`data/db/`) holds six tables: trip entries ("Входная информация"), rental records
  ("База данных" — this single table also carries the extended check-in fields ЖК/подъезд/этаж/
  Wi-Fi/etc. shown in full by "Информация по аренде"), landlords, resident employees, cities, and
  the independent finance-approval registry.
- **Cross-screen sync** ("Сквозная синхронизация полей" in the spec) is a single
  `SharedFieldsRepository` (`data/shared/`) — an app-wide `StateFlow<SharedFields>` bus, persisted
  to DataStore as JSON, that the guest list, city, address, ЖК, apartment type, dates, times, and
  rate/deposit fields all read from and write to directly. Editing any of them on one screen is
  instantly visible on every other screen that shows it, with no "save" step.
- **Editable text templates** ("Информация по заселению", "Бронирование", finance objects) live in
  `TemplateStore` (DataStore-backed) and are rendered through `TemplateEngine`, a plain
  `{token}` → value substitution.
- **Excel** (`util/excel/`) is Apache POI for both read and write, plus a small hand-rolled CSV
  reader so "Импорт из Excel/CSV" also accepts `.csv`. Column matching for import is exact-text
  first, then case-insensitive substring, per spec.
- All "today"/date-math (dashboard, calendar) goes through `TimeUtils`, pinned to `Asia/Almaty`
  (UTC+5) regardless of the device's own time zone.

## Screen → code map

| # | Spec section | Package |
|---|---|---|
| 1 | Входная информация | `ui/screens/trip` |
| 2 | Дэшборд | `ui/screens/dashboard` |
| 3 | Календарь | `ui/screens/calendarview` |
| 4 | Информация по заселению | `ui/screens/checkin` |
| 5 | Бронирование | `ui/screens/booking` |
| 6 | Согласование с финансами | `ui/screens/finance` |
| 7 | База данных | `ui/screens/database` |
| 8 | Информация по аренде | `ui/screens/rentalinfo` |
| 9 | Арендодатели | `ui/screens/landlords` |
| 10 | Проживающие сотрудники | `ui/screens/residents` |
| 11 | Города | `ui/screens/cities` |

## Notable implementation decisions / simplifications

- **Section 4 vs. Section 7/8 field ownership.** The spec's explicit sync list doesn't include the
  подъезд/этаж/домофон/Wi-Fi/2ГИС/правила-проживания fields, so those live only on "Информация по
  заселению" as a local (non-persisted-elsewhere) generator form, while the same fields also exist
  as directly-editable columns in "База данных" / "Информация по аренде" for a saved rental
  record. Only the fields the spec explicitly lists as synced (guest names, city, address, ЖК,
  apartment type, dates, times, rate, deposit) flow through the shared bus.
- **"Бронирование" and "Согласование с финансами"** are, per spec, generators/registries rather
  than their own persisted master table — their fields (other than the finance registry) live on
  the shared bus / in-memory ViewModel state, not as separate Room rows.
- **Corporate trip-system import** (Screen 7's second import button) is wired up end-to-end with
  the same header-matching machinery as the generic import, but the real corporate export's exact
  column headers aren't available yet — see the doc comment on
  `toTripEntryEntityFromCorporateFormat()` in `util/excel/TripEntryExcel.kt` for the one place to
  add them once a sample file exists.
- **"Удалить все данные"** clears every table (trip entries, rental records, landlords, resident
  employees, finance registry) and resets the city list back to the default 20, matching the
  spec's "не только эту таблицу, а вообще всё".
