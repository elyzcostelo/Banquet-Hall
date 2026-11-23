package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ReservationEntity
import com.example.model.BookingStatus
import com.example.model.DeckDate
import com.example.ui.theme.BrassGold
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusCrimson
import com.example.ui.theme.StatusEmerald
import com.example.ui.viewmodel.BanquetUiState
import com.example.ui.viewmodel.CoordinatorFilter

@Composable
fun CoordinatorScreen(
    uiState: BanquetUiState,
    onFilterChange: (CoordinatorFilter) -> Unit,
    onPlaceMaintenanceHold: () -> Unit,
    onUpdateStatus: (ReservationEntity, BookingStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filtered = remember(uiState.allReservations, uiState.coordinatorFilter) {
        uiState.allReservations.filter { item ->
            when (uiState.coordinatorFilter) {
                CoordinatorFilter.ALL -> true
                CoordinatorFilter.CONFIRMED -> item.status == BookingStatus.CONFIRMED
                CoordinatorFilter.CHECKED_IN -> item.status == BookingStatus.CHECKED_IN
                CoordinatorFilter.MAINTENANCE -> item.status == BookingStatus.MAINTENANCE_HOLD
                CoordinatorFilter.CANCELLED -> item.status == BookingStatus.CANCELLED
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("coordinator_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = null,
                            tint = BrassGold,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp),
                        )
                    }
                    Column {
                        Text(
                            text = "Coordinator Desk",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "June V Building • 5th Deck Operations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // KPI Summary Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CoordinatorMetricCard(
                        label = "Active",
                        value = uiState.activeCount.toString(),
                        accent = StatusEmerald,
                        modifier = Modifier.weight(1f),
                    )
                    CoordinatorMetricCard(
                        label = "Checked In",
                        value = uiState.checkedInCount.toString(),
                        accent = BrassGold,
                        modifier = Modifier.weight(1f),
                    )
                    CoordinatorMetricCard(
                        label = "Holds",
                        value = uiState.maintenanceHoldCount.toString(),
                        accent = StatusAmber,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Quick Maintenance Hold Card
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    ),
                    border = BorderStroke(1.dp, BrassGold.copy(alpha = 0.45f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Selected: ${uiState.selectedDate.formatFullDisplay()}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Lock a session for rigging, cleaning, or VIP prep",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onPlaceMaintenanceHold,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("place_hold_button"),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Build,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hold Slot")
                        }
                    }
                }

                // Filter Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CoordinatorFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = uiState.coordinatorFilter == filter,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.testTag("coordinator_filter_${filter.name.lowercase()}"),
                        )
                    }
                }
            }
        }

        items(items = filtered, key = { it.id }) { reservation ->
            CoordinatorItemCard(
                reservation = reservation,
                onCheckIn = { onUpdateStatus(reservation, BookingStatus.CHECKED_IN) },
                onCancel = { onUpdateStatus(reservation, BookingStatus.CANCELLED) },
                modifier = Modifier.widthIn(max = 680.dp),
            )
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun CoordinatorMetricCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = accent,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CoordinatorItemCard(
    reservation: ReservationEntity,
    onCheckIn: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val parsedDate = DeckDate.parseIso(reservation.dateIso)
    val badgeColor = when (reservation.status) {
        BookingStatus.CONFIRMED -> StatusEmerald
        BookingStatus.CHECKED_IN -> BrassGold
        BookingStatus.MAINTENANCE_HOLD -> StatusAmber
        BookingStatus.CANCELLED -> StatusCrimson
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("coordinator_card_${reservation.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${reservation.referenceCode} • ${parsedDate?.formatShortMonthDay() ?: reservation.dateIso}",
                    style = MaterialTheme.typography.labelMedium,
                    color = BrassGold,
                    fontWeight = FontWeight.Bold,
                )

                Surface(
                    color = badgeColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        text = reservation.status.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Text(
                text = reservation.eventTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "${reservation.sessionBlock.displayName} (${reservation.sessionBlock.timeRangeLabel}) • ${reservation.organizerName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (reservation.status != BookingStatus.CANCELLED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (reservation.status == BookingStatus.CONFIRMED) {
                        Button(
                            onClick = onCheckIn,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("checkin_button_${reservation.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusEmerald,
                            ),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HowToReg,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.action_check_in))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    } else if (reservation.status == BookingStatus.CHECKED_IN) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = BrassGold,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "On Deck",
                                style = MaterialTheme.typography.labelMedium,
                                color = BrassGold,
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("coordinator_cancel_${reservation.id}"),
                        border = BorderStroke(1.dp, StatusCrimson.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = null,
                            tint = StatusCrimson,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (reservation.status == BookingStatus.MAINTENANCE_HOLD) {
                                "Release Hold"
                            } else {
                                "Cancel"
                            },
                            color = StatusCrimson,
                        )
                    }
                }
            }
        }
    }
}
