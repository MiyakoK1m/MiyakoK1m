package com.hrhousing.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Editable, growable list of full-name fields (used for "ФИО проживающих" / "ФИО гостей" — the
 * spec asks for one plain text field per person, with add/remove buttons, not a chip input).
 */
@Composable
fun DynamicNameList(
    names: List<String>,
    onChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "ФИО",
) {
    Column(modifier = modifier.fillMaxWidth()) {
        names.forEachIndexed { index, name ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { newValue -> onChange(names.toMutableList().also { it[index] = newValue }) },
                    label = { Text("$label ${index + 1}") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                if (names.size > 1) {
                    IconButton(onClick = { onChange(names.toMutableList().also { it.removeAt(index) }) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Удалить")
                    }
                }
            }
        }
        TextButton(onClick = { onChange(names + "") }) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Добавить ещё одного")
        }
    }
}
