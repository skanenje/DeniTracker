package com.swapo.paymentracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swapo.paymentracker.data.PaymentDatabase
import com.swapo.paymentracker.data.PaymentEntry
import kotlin.math.abs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PersonTotal(
    val name: String,
    val net: Double,
    val entries: List<PaymentEntry>
)

class PaymentViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = PaymentDatabase.get(app).paymentDao()

    private fun <T> Flow<T>.asState(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)

    val entries: StateFlow<List<PaymentEntry>> =
        dao.observeAll().asState(emptyList())

    val owedToMeTotal: StateFlow<Double> =
        entries.map { list ->
            list.filter { !it.settled && it.owedToMe }.sumOf { it.amount }
        }.asState(0.0)

    val iOweTotal: StateFlow<Double> =
        entries.map { list ->
            list.filter { !it.settled && !it.owedToMe }.sumOf { it.amount }
        }.asState(0.0)

    val people: StateFlow<List<PersonTotal>> =
        entries.map { list ->
            list.groupBy { it.person.trim().lowercase() }
                .map { (_, items) ->
                    PersonTotal(
                        name = items.first().person.trim(),
                        net = items.filter { !it.settled }
                            .sumOf { if (it.owedToMe) it.amount else -it.amount },
                        entries = items
                    )
                }
                .sortedByDescending { abs(it.net) }
        }.asState(emptyList())

    fun add(date: String, person: String, owedToMe: Boolean, amount: Double, note: String) {
        viewModelScope.launch {
            dao.insert(
                PaymentEntry(
                    date = date,
                    person = person.trim(),
                    owedToMe = owedToMe,
                    amount = amount,
                    note = note.trim()
                )
            )
        }
    }

    fun toggleSettled(entry: PaymentEntry) {
        viewModelScope.launch { dao.update(entry.copy(settled = !entry.settled)) }
    }

    fun delete(entry: PaymentEntry) {
        viewModelScope.launch { dao.delete(entry) }
    }
}