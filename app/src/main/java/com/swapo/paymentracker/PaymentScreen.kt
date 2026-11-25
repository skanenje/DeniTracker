package com.swapo.paymentracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.swapo.paymentracker.data.PaymentEntry
import java.time.LocalDate
import java.util.Locale

private val OwedColor = Color(0xFF1C7A4B)
private val OweColor = Color(0xFFB3382C)

private fun money(n: Double) = "KES " + String.format(Locale.US, "%,.2f", n)

@Composable
fun PaymentScreen(vm: PaymentViewModel) {
    val entries by vm.entries.collectAsState()
    val owedToMe by vm.owedToMeTotal.collectAsState()
    val iOwe by vm.iOweTotal.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAdd = true }) {
                Text("+ Add entry")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Who owes who",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TotalBox("Owed to you", owedToMe, OwedColor, Modifier.weight(1f))
                TotalBox("You owe", iOwe, OweColor, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            if (entries.isEmpty()) {
                Text("No entries yet. Tap “+ Add entry” to record one.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(entries, key = { it.id }) { e ->
                        EntryRow(
                            e,
                            onSettle = { vm.toggleSettled(e) },
                            onDelete = { vm.delete(e) }
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddDialog(
            onDismiss = { showAdd = false },
            onSave = { date, person, owed, amount, note ->
                vm.add(date, person, owed, amount, note)
                showAdd = false
            }
        )
    }
}

@Composable
fun TotalBox(label: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(money(amount), style = MaterialTheme.typography.titleMedium, color = color)
        }
    }
}

@Composable
fun EntryRow(e: PaymentEntry, onSettle: () -> Unit, onDelete: () -> Unit) {
    val color = if (e.owedToMe) OwedColor else OweColor
    val sub = buildString {
        append(e.date)
        append(if (e.owedToMe) " · owes you" else " · you owe")
        if (e.note.isNotBlank()) append(" · ${e.note}")
        if (e.settled) append(" · settled")
    }
    Card(Modifier.fillMaxWidth().alpha(if (e.settled) 0.5f else 1f)) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(e.person, style = MaterialTheme.typography.titleMedium)
                    Text(sub, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    (if (e.owedToMe) "+" else "−") + money(e.amount),
                    color = color,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Row {
                TextButton(onClick = onSettle) { Text(if (e.settled) "Reopen" else "Mark settled") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
fun AddDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean, Double, String) -> Unit
) {
    var owedToMe by remember { mutableStateOf(true) }
    var person by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New entry") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (owedToMe) {
                        Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("They owe me") }
                        OutlinedButton(onClick = { owedToMe = false }, modifier = Modifier.weight(1f)) { Text("I owe them") }
                    } else {
                        OutlinedButton(onClick = { owedToMe = true }, modifier = Modifier.weight(1f)) { Text("They owe me") }
                        Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("I owe them") }
                    }
                }
                OutlinedTextField(
                    value = person,
                    onValueChange = { person = it },
                    label = { Text("Person") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (KES)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (yyyy-mm-dd)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true
                )
                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.trim().toDoubleOrNull()
                val dateOk = runCatching { LocalDate.parse(date.trim()) }.isSuccess
                when {
                    person.isBlank() -> error = "Enter the person’s name."
                    amt == null || amt <= 0.0 -> error = "Enter an amount above zero."
                    !dateOk -> error = "Date must look like 2026-11-25."
                    else -> onSave(date.trim(), person, owedToMe, amt, note)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}