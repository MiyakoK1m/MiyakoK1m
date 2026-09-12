package com.hrhousing.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

/** A table cell that shows truncated text with "…" and expands/collapses in place on tap. */
@Composable
fun ExpandableCell(text: String, modifier: Modifier = Modifier, collapsedMaxLines: Int = 1) {
    var expanded by remember { mutableStateOf(false) }
    Text(
        text = text,
        maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.clickable { expanded = !expanded },
    )
}
