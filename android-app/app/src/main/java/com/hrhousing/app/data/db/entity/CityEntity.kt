package com.hrhousing.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

/** The 20 largest Kazakhstan cities the "Города" screen is pre-populated with. */
val DEFAULT_CITIES = listOf(
    "Астана", "Алматы", "Шымкент", "Караганда", "Актобе", "Атырау",
    "Усть-Каменогорск", "Тараз", "Талдыкорган", "Уральск", "Костанай",
    "Кызылорда", "Актау", "Павлодар", "Петропавловск", "Туркестан",
    "Жезказган", "Конаев", "Семей", "Кокшетау",
)

/** ~20 major Kazakhstan airport IATA codes used on the rental form. */
val AIRPORT_CODES = listOf(
    "NQZ — Астана", "ALA — Алматы", "CIT — Шымкент", "KGF — Караганда",
    "AKX — Актобе", "GUW — Атырау", "UKK — Усть-Каменогорск", "DMB — Тараз",
    "TDK — Талдыкорган", "URA — Уральск", "KSN — Костанай", "KZO — Кызылорда",
    "SCO — Актау", "PWQ — Павлодар", "PPK — Петропавловск", "HSA — Туркестан",
    "DZN — Жезказган", "GYD — Конаев", "PLX — Семей", "KOV — Кокшетау",
)
