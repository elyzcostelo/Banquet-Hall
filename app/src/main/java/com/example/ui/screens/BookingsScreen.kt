package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.viewmodel.BookingsFilter

@Composable
fun BookingsScreen(
    uiState: BanquetUiState,
    onSearchChange: (String) -> Unit,
    onFilterChange: (BookingsFilter) -> Unit,
    onCancelBooking: (ReservationEntity) -> Unit,
    onJumpToDate: (DeckDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingCancelTarget by remember { mutableStateOf<ReservationEntity?>(null) }

    val filtered = remember(uiState.allReservations, uiState.bookingsFilter, uiState.searchQuery, uiState.today) {
        val todayIso = uiState.today.toIsoString()
        val query = uiState.searchQuery.trim().lowercase()
        uiState.allReservations
            .filter { item ->
                when (uiState.bookingsFilter) {
                    BookingsFilter.UPCOMING -> item.status != BookingStatus.CANCELLED && item.dateIso >= todayIso
                    BookingsFilter.PAST -> item.status != BookingStatus.CANCELLED && item.dateIso < todayIso
                    BookingsFilter.CANCELLED -> item.status == BookingStatus.CANCELLED
                }
            }
            .filter { item ->
                if (query.isEmpty()) {
                    true
                } else {
                    item.eventTitle.lowercase().contains(query) ||
                        item.department.lowercase().contains(query) ||
                        item.organizerName.lowercase().contains(query) ||
                        item.referenceCode.lowercase().contains(query)
                }
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("bookings_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "5th Deck Reservations",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search event, department, organizer, or code") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bookings_search_input"),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BookingsFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = uiState.bookingsFilter == filter,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.testTag("bookings_filter_${filter.name.lowercase()}"),
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyBookingsState(
                    filter = uiState.bookingsFilter,
                    modifier = Modifier.widthIn(max = 640.dp),
                )
            }
        } else {
            items(items = filtered, key = { it.id }) { reservation ->
                ReservationItemCard(
                    reservation = reservation,
                    canCancel = reservation.status != BookingStatus.CANCELLED &&
                        reservation.dateIso >= uiState.today.toIsoString(),
                    onInspectOnCalendar = {
                        DeckDate.parseIso(reservation.dateIso)?.let(onJumpToDate)
                    },
                    onRequestCancel = { pendingCancelTarget = reservation },
                    modifier = Modifier.widthIn(max = 640.dp),
                )
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    pendingCancelTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingCancelTarget = null },
            title = { Text("Cancel Reservation?") },
            text = {
                Text(
                    "Cancel \"${target.eventTitle}\" (${target.referenceCode})? This immediately reopens the ${target.sessionBlock.displayName} slot on the real-time calendar.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancelBooking(target)
                        pendingCancelTarget = null
                    },
                    modifier = Modifier.testTag("confirm_cancel_dialog_button"),
                ) {
                    Text("Cancel Booking", color = StatusCrimson)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingCancelTarget = null }) {
                    Text("Keep")
                }
            },
        )
    }
}

@Composable
private fun ReservationItemCard(
    reservation: ReservationEntity,
    canCancel: Boolean,
    onInspectOnCalendar: () -> Unit,
    onRequestCancel: () -> Unit,
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
            .testTag("booking_card_${reservation.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = reservation.referenceCode,
                    style = MaterialTheme.typography.labelMedium,
                    color = BrassGold,
                    fontWeight = FontWeight.Bold,
                )

                Surface(
                    color = badgeColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.45f)),
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Event,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "${parsedDate?.formatFullDisplay() ?: reservation.dateIso} • ${reservation.sessionBlock.displayName} (${reservation.sessionBlock.timeRangeLabel})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }

            Text(
                text = "${reservation.organizerName} • ${reservation.department}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (reservation.guestCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${reservation.guestCount} guests • ${reservation.setupStyle.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (reservation.specialNotes.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = "Notes: ${reservation.specialNotes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onInspectOnCalendar,
                    modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                    Text("View on Calendar")
                }

                if (canCancel) {
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(
                        onClick = onRequestCancel,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("cancel_booking_${reservation.id}"),
                        border = BorderStroke(1.dp, StatusCrimson.copy(alpha = 0.55f)),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = null,
                            tint = StatusCrimson,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.action_cancel_booking),
                            color = StatusCrimson,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBookingsState(
    filter: BookingsFilter,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_empty_deck),
                contentDescription = stringResource(R.string.empty_day_cd),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop,
            )

            Text(
                text = when (filter) {
                    BookingsFilter.UPCOMING -> "No Upcoming Deck Bookings"
                    BookingsFilter.PAST -> "No Past Events Archived"
                    BookingsFilter.CANCELLED -> "No Cancelled Reservations"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Select any open session block on the Calendar tab to reserve the June V Building 5th Deck Banquet Hall.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
