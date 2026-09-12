package com.hrhousing.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * A text field with suggestions from [options] filtered as the user types. Free text is never
 * blocked — picking a suggestion just fills the field, it doesn't restrict what can be typed.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AutosuggestTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = remember(value, options) {
        if (value.isBlank()) options else options.filter { it.contains(value, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(expanded = expanded && filtered.isNotEmpty(), onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onChange(it)
                expanded = true
            },
            label = { Text(label) },
            modifier = modifier.menuAnchor(),
            singleLine = true,
        )
        DropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false },
            properties = androidx.compose.ui.window.PopupProperties(focusable = false),
        ) {
            filtered.take(8).forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onChange(option)
                    expanded = false
                })
            }
        }
    }
}
