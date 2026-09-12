package com.hrhousing.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** Floating "scroll to top / scroll to bottom" buttons for long lists (spec requires these). */
@Composable
fun ScrollFabs(listState: LazyListState, itemCount: Int, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    Column(modifier = modifier.padding(16.dp)) {
        FloatingActionButton(
            onClick = { scope.launch { listState.animateScrollToItem(0) } },
            modifier = Modifier.size(48.dp),
        ) { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "В начало") }

        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))

        FloatingActionButton(
            onClick = { scope.launch { if (itemCount > 0) listState.animateScrollToItem(itemCount - 1) } },
            modifier = Modifier.size(48.dp),
        ) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "В конец") }
    }
}
