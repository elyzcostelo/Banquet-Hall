package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DeckDate
import com.example.model.SessionBlock
import com.example.ui.components.CalendarGridCard
import com.example.ui.components.LiveDeckHeader
import com.example.ui.components.SlotTimelineCard
import com.example.ui.theme.BrassGold
import com.example.ui.viewmodel.BanquetUiState

@Composable
fun CalendarScreen(
    uiState: BanquetUiState,
    onSelectDate: (DeckDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToday: () -> Unit,
    onReserveSlot: (DeckDate, SessionBlock) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen"),
    ) {
        val isWideLayout = maxWidth >= 720.dp

        if (isWideLayout) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    LiveDeckHeader(liveStatus = uiState.liveDeckStatus)
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        CalendarGridCard(
                            displayedMonth = uiState.displayedMonth,
                            selectedDate = uiState.selectedDate,
                            today = uiState.today,
                            monthOccupancy = uiState.monthOccupancy,
                            onSelectDate = onSelectDate,
                            onPreviousMonth = onPreviousMonth,
                            onNextMonth = onNextMonth,
                            onJumpToday = onJumpToday,
                            modifier = Modifier.weight(1.05f),
                        )

                        Column(
                            modifier = Modifier.weight(0.95f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            SelectedDateHeader(
                                selectedDate = uiState.selectedDate,
                                today = uiState.today,
                            )
                            uiState.selectedDaySlots.forEach { slot ->
                                SlotTimelineCard(
                                    slot = slot,
                                    onReserveClick = {
                                        onReserveSlot(uiState.selectedDate, slot.sessionBlock)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    LiveDeckHeader(
                        liveStatus = uiState.liveDeckStatus,
                        modifier = Modifier.widthIn(max = 600.dp),
                    )
                }

                item {
                    CalendarGridCard(
                        displayedMonth = uiState.displayedMonth,
                        selectedDate = uiState.selectedDate,
                        today = uiState.today,
                        monthOccupancy = uiState.monthOccupancy,
                        onSelectDate = onSelectDate,
                        onPreviousMonth = onPreviousMonth,
                        onNextMonth = onNextMonth,
                        onJumpToday = onJumpToday,
                        modifier = Modifier.widthIn(max = 600.dp),
                    )
                }

                item {
                    SelectedDateHeader(
                        selectedDate = uiState.selectedDate,
                        today = uiState.today,
                        modifier = Modifier.widthIn(max = 600.dp),
                    )
                }

                items(
                    items = uiState.selectedDaySlots,
                    key = { it.sessionBlock.name },
                ) { slot ->
                    SlotTimelineCard(
                        slot = slot,
                        onReserveClick = {
                            onReserveSlot(uiState.selectedDate, slot.sessionBlock)
                        },
                        modifier = Modifier.widthIn(max = 600.dp),
                    )
                }

                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
private fun SelectedDateHeader(
    selectedDate: DeckDate,
    today: DeckDate,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = selectedDate.formatLongDisplay(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = when {
                    selectedDate == today -> "Today • Real-time deck availability"
                    selectedDate < today -> "Past date • Read-only archive"
                    else -> "Session blocks • Instant confirmation"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (selectedDate == today) BrassGold else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
