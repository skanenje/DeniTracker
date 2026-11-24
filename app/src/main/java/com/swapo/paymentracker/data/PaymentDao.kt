package com.swapo.paymentracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Insert
    suspend fun insert(entry: PaymentEntry)

    @Update
    suspend fun update(entry: PaymentEntry)

    @Delete
    suspend fun delete(entry: PaymentEntry)

    @Query("SELECT * FROM entries ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<PaymentEntry>>
}