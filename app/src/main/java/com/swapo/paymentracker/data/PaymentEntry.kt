package com.swapo.paymentracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class PaymentEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val person: String,
    val owedToMe: Boolean,
    val amount: Double,
    val note: String = "",
    val settled: Boolean = false
)