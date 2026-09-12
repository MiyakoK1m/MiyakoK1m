package com.hrhousing.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OwnerType { OWNER, AGENT }
enum class PaymentMethod { INVOICE, TRANSFER }

@Entity(tableName = "landlords")
data class LandlordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phone: String = "",
    val ownerType: OwnerType = OwnerType.OWNER,
    val city: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.TRANSFER,
    val rating: Int = 7,
    val comment: String = "",
)
