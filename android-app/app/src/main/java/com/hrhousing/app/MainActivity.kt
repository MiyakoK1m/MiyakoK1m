package com.hrhousing.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.hrhousing.app.ui.nav.AppScaffold
import com.hrhousing.app.ui.theme.HrHousingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as HrHousingApp).container

        setContent {
            val isDark by container.themePreferences.isDarkTheme.collectAsState()
            HrHousingTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppScaffold(container)
                }
            }
        }
    }
}
