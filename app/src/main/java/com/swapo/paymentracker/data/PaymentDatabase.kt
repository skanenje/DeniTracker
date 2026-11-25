package com.swapo.paymentracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PaymentEntry::class], version = 1, exportSchema = false)
abstract class PaymentDatabase : RoomDatabase() {

    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var instance: PaymentDatabase? = null

        fun get(context: Context): PaymentDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PaymentDatabase::class.java,
                    "payments.db"
                ).build().also { instance = it }
            }
    }
}