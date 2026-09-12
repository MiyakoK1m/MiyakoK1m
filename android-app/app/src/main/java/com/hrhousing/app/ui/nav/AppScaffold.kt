package com.hrhousing.app.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hrhousing.app.AppContainer
import com.hrhousing.app.ui.screens.booking.BookingScreen
import com.hrhousing.app.ui.screens.calendarview.CalendarScreen
import com.hrhousing.app.ui.screens.checkin.CheckinInfoScreen
import com.hrhousing.app.ui.screens.cities.CitiesScreen
import com.hrhousing.app.ui.screens.dashboard.DashboardScreen
import com.hrhousing.app.ui.screens.database.RentalDatabaseScreen
import com.hrhousing.app.ui.screens.finance.FinanceApprovalScreen
import com.hrhousing.app.ui.screens.landlords.LandlordsScreen
import com.hrhousing.app.ui.screens.rentalinfo.RentalInfoTableScreen
import com.hrhousing.app.ui.screens.residents.ResidentsScreen
import com.hrhousing.app.ui.screens.settings.SettingsScreen
import com.hrhousing.app.ui.screens.trip.TripInputScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(container: AppContainer) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isDark by container.themePreferences.isDarkTheme.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Заселение и аренда",
                    modifier = Modifier.padding(16.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                )
                Screen.values().forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(screen.title) },
                        icon = { Icon(screen.icon, contentDescription = null) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(Screen.values().firstOrNull { it.route == currentRoute }?.title ?: "Заселение и аренда") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Меню")
                        }
                    },
                    actions = {
                        IconButton(onClick = { container.themePreferences.toggle() }) {
                            Icon(
                                if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = "Тема",
                            )
                        }
                    },
                )
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.TripInput.route,
                modifier = Modifier.padding(padding),
            ) {
                composable(Screen.TripInput.route) { TripInputScreen(container) }
                composable(Screen.Dashboard.route) { DashboardScreen(container) }
                composable(Screen.Calendar.route) { CalendarScreen(container) }
                composable(Screen.CheckinInfo.route) { CheckinInfoScreen(container) }
                composable(Screen.Booking.route) { BookingScreen(container) }
                composable(Screen.FinanceApproval.route) { FinanceApprovalScreen(container) }
                composable(Screen.RentalDatabase.route) { RentalDatabaseScreen(container) }
                composable(Screen.RentalInfoTable.route) { RentalInfoTableScreen(container) }
                composable(Screen.Landlords.route) { LandlordsScreen(container) }
                composable(Screen.Residents.route) { ResidentsScreen(container) }
                composable(Screen.Cities.route) { CitiesScreen(container) }
                composable(Screen.Settings.route) { SettingsScreen(container) }
            }
        }
    }
}
