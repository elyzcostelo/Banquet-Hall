package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.BookmarkAdded
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.SessionBlock
import com.example.ui.components.BookingBottomSheet
import com.example.ui.components.BookingPassDialog
import com.example.ui.screens.BookingsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CoordinatorScreen
import com.example.ui.screens.VenueInfoScreen
import com.example.ui.theme.AimsBanquetTheme
import com.example.ui.viewmodel.BanquetViewModel

private enum class DeckDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
) {
    CALENDAR(
        route = "calendar",
        labelRes = R.string.nav_calendar,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth,
        testTag = "nav_calendar",
    ),
    BOOKINGS(
        route = "bookings",
        labelRes = R.string.nav_bookings,
        selectedIcon = Icons.Filled.BookmarkAdded,
        unselectedIcon = Icons.Outlined.BookmarkAdded,
        testTag = "nav_bookings",
    ),
    COORDINATOR(
        route = "coordinator",
        labelRes = R.string.nav_coordinator,
        selectedIcon = Icons.Filled.AdminPanelSettings,
        unselectedIcon = Icons.Outlined.AdminPanelSettings,
        testTag = "nav_coordinator",
    ),
    VENUE(
        route = "venue",
        labelRes = R.string.nav_venue,
        selectedIcon = Icons.Filled.Apartment,
        unselectedIcon = Icons.Outlined.Apartment,
        testTag = "nav_venue",
    ),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AimsBanquetTheme {
                AimsBanquetApp()
            }
        }
    }
}

@Composable
fun AimsBanquetApp(
    viewModel: BanquetViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bookingSheetConfig by viewModel.bookingSheetConfig.collectAsStateWithLifecycle()
    val confirmedPass by viewModel.confirmedPass.collectAsStateWithLifecycle()
    val formError by viewModel.formError.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: DeckDestination.CALENDAR.route
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        val message = feedbackMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeFeedbackMessage()
    }

    val navigateTo: (DeckDestination) -> Unit = { dest ->
        navController.navigate(dest.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val firstBookableSession = remember(uiState.selectedDaySlots) {
        uiState.selectedDaySlots.firstOrNull { it.isBookable }?.sessionBlock ?: SessionBlock.MORNING
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 720.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if (currentRoute == DeckDestination.CALENDAR.route && uiState.selectedDate >= uiState.today) {
                    FloatingActionButton(
                        onClick = {
                            viewModel.openBookingSheet(
                                date = uiState.selectedDate,
                                session = firstBookableSession,
                                isMaintenanceHold = false,
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.testTag("fab_quick_reserve"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = "Quick reserve selected date",
                        )
                    }
                }
            },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        DeckDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(dest) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes),
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        DeckDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTo(dest) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes),
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag),
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = DeckDestination.CALENDAR.route,
                    modifier = Modifier.weight(1f),
                ) {
                    composable(DeckDestination.CALENDAR.route) {
                        CalendarScreen(
                            uiState = uiState,
                            onSelectDate = viewModel::selectDate,
                            onPreviousMonth = viewModel::previousMonth,
                            onNextMonth = viewModel::nextMonth,
                            onJumpToday = viewModel::jumpToToday,
                            onReserveSlot = { date, session ->
                                viewModel.openBookingSheet(
                                    date = date,
                                    session = session,
                                    isMaintenanceHold = false,
                                )
                            },
                        )
                    }
                    composable(DeckDestination.BOOKINGS.route) {
                        BookingsScreen(
                            uiState = uiState,
                            onSearchChange = viewModel::updateSearchQuery,
                            onFilterChange = viewModel::setBookingsFilter,
                            onCancelBooking = { reservation ->
                                viewModel.updateReservationStatus(
                                    reservation,
                                    com.example.model.BookingStatus.CANCELLED,
                                )
                            },
                            onJumpToDate = { date ->
                                viewModel.selectDate(date)
                                navigateTo(DeckDestination.CALENDAR)
                            },
                        )
                    }
                    composable(DeckDestination.COORDINATOR.route) {
                        CoordinatorScreen(
                            uiState = uiState,
                            onFilterChange = viewModel::setCoordinatorFilter,
                            onPlaceMaintenanceHold = {
                                val targetDate = if (uiState.selectedDate >= uiState.today) {
                                    uiState.selectedDate
                                } else {
                                    uiState.today
                                }
                                viewModel.openBookingSheet(
                                    date = targetDate,
                                    session = firstBookableSession,
                                    isMaintenanceHold = true,
                                )
                            },
                            onUpdateStatus = viewModel::updateReservationStatus,
                        )
                    }
                    composable(DeckDestination.VENUE.route) {
                        VenueInfoScreen(
                            onReserveNowClick = { navigateTo(DeckDestination.CALENDAR) },
                        )
                    }
                }
            }
        }
    }

    bookingSheetConfig?.let { config ->
        BookingBottomSheet(
            config = config,
            daySlots = uiState.selectedDaySlots,
            formError = formError,
            onDismiss = viewModel::closeBookingSheet,
            onClearError = viewModel::clearFormError,
            onSubmit = viewModel::submitBooking,
        )
    }

    confirmedPass?.let { reservation ->
        BookingPassDialog(
            reservation = reservation,
            onDismiss = viewModel::dismissConfirmedPass,
        )
    }
}
