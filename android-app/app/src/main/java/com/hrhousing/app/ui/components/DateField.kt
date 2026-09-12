package com.hrhousing.app.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hrhousing.app.util.TimeUtils
import java.time.LocalDate

@Composable
fun DateField(
    label: String,
    value: LocalDate?,
    onChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    OutlinedTextField(
        value = TimeUtils.format(value),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier,
        trailingIcon = {
            IconButton(onClick = {
                val base = value ?: TimeUtils.today()
                DatePickerDialog(
                    context,
                    { _, year, month, day -> onChange(LocalDate.of(year, month + 1, day)) },
                    base.year, base.monthValue - 1, base.dayOfMonth,
                ).show()
            }) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Выбрать дату")
            }
        },
    )
}

@Composable
fun TimeTextField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        placeholder = { Text("14:00") },
    )
}

@Composable
fun MoneyField(label: String, value: Long, onChange: (Long) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = if (value == 0L) "" else value.toString(),
        onValueChange = { text -> onChange(text.filter { it.isDigit() }.toLongOrNull() ?: 0L) },
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        suffix = { Text("₸") },
    )
}
