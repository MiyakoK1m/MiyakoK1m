package com.hrhousing.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private const val CONFIRM_WORD = "УДАЛИТЬ"

/** Double-confirmation for the destructive "Удалить все данные" action: a dialog + typed word. */
@Composable
fun ConfirmDeleteAllDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    var typed by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить ВСЕ данные приложения?") },
        text = {
            Column {
                Text("Это удалит базу аренды, входную информацию, арендодателей и все остальные разделы без возможности восстановления. Чтобы подтвердить, введите слово \"$CONFIRM_WORD\".")
                OutlinedTextField(value = typed, onValueChange = { typed = it }, label = { Text(CONFIRM_WORD) })
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = typed.trim().equals(CONFIRM_WORD, ignoreCase = false)) {
                Text("Удалить всё")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}
