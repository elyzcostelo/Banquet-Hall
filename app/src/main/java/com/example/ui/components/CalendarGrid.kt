package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.DayOccupancyState
import com.example.domain.DayOccupancySummary
import com.example.model.DeckDate
import com.example.model.DeckYearMonth
import com.example.ui.theme.BrassGold
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusCrimson
import com.example.ui.theme.StatusEmerald

private val WEEKDAY_LABELS = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

@Composable
fun CalendarGridCard(
    displayedMonth: DeckYearMonth,
    selectedDate: DeckDate,
    today: DeckDate,
    monthOccupancy: Map<Int, DayOccupancySummary>,
    onSelectDate: (DeckDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calendar_grid_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Month Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = displayedMonth.formatMonthYear(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Tap any date to inspect session blocks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FilledTonalButton(
                        onClick = onJumpToday,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("today_jump_button"),
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = stringResource(R.string.action_jump_today),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }

                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("prev_month_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.prev_month_cd),
                        )
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("next_month_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.next_month_cd),
                        )
                    }
                }
            }

            // Weekday Header Row
            Row(modifier = Modifier.fillMaxWidth()) {
                WEEKDAY_LABELS.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Calendar Cells
            val totalDays = displayedMonth.daysInMonth()
            val leadingOffset = displayedMonth.firstDayOfWeekZeroBased()
            val totalCells = ((leadingOffset + totalDays + 6) / 7) * 7

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (rowStart in 0 until totalCells step 7) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = rowStart + col
                            val dayNum = cellIndex - leadingOffset + 1
                            if (dayNum in 1..totalDays) {
                                val cellDate = displayedMonth.atDay(dayNum)
                                val summary = monthOccupancy[dayNum]
                                CalendarDayTile(
                                    day = dayNum,
                                    isSelected = cellDate == selectedDate,
                                    isToday = cellDate == today,
                                    summary = summary,
                                    onClick = { onSelectDate(cellDate) },
                                    modifier = Modifier.weight(1f),
                                )
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Legend Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LegendPill(color = StatusEmerald, label = "Open")
                LegendPill(color = BrassGold, label = "Partial")
                LegendPill(color = StatusCrimson, label = "Full")
                LegendPill(color = StatusAmber, label = "Hold")
            }
        }
    }
}

@Composable
private fun CalendarDayTile(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    summary: DayOccupancySummary?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = summary?.state ?: DayOccupancyState.OPEN
    val isPast = state == DayOccupancyState.PAST

    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            state == DayOccupancyState.FULL -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
            state == DayOccupancyState.MAINTENANCE -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
            state == DayOccupancyState.PARTIAL -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else -> Color.Transparent
        },
        label = "day_tile_bg",
    )

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderStroke = when {
        isToday && !isSelected -> BorderStroke(1.5.dp, BrassGold)
        isSelected -> BorderStroke(1.5.dp, BrassGold)
        else -> null
    }

    Surface(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .alpha(if (isPast && !isSelected) 0.62f else 1f)
            .testTag("calendar_day_$day"),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = borderStroke,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
            )

            // 3 Session Dots: Morning, Afternoon, Evening
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SessionMicroDot(
                    reserved = summary?.morningReserved == true,
                    isMaintenance = summary?.hasMaintenanceHold == true && state == DayOccupancyState.MAINTENANCE,
                    isSelected = isSelected,
                    isPast = isPast,
                )
                SessionMicroDot(
                    reserved = summary?.afternoonReserved == true,
                    isMaintenance = summary?.hasMaintenanceHold == true && state == DayOccupancyState.MAINTENANCE,
                    isSelected = isSelected,
                    isPast = isPast,
                )
                SessionMicroDot(
                    reserved = summary?.eveningReserved == true,
                    isMaintenance = summary?.hasMaintenanceHold == true && state == DayOccupancyState.MAINTENANCE,
                    isSelected = isSelected,
                    isPast = isPast,
                )
            }
        }
    }
}

@Composable
private fun SessionMicroDot(
    reserved: Boolean,
    isMaintenance: Boolean,
    isSelected: Boolean,
    isPast: Boolean,
) {
    val dotColor = when {
        isPast && !reserved -> Color.Transparent
        isMaintenance -> StatusAmber
        reserved && isSelected -> BrassGold
        reserved -> StatusCrimson
        isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f)
        else -> StatusEmerald.copy(alpha = 0.55f)
    }

    Box(
        modifier = Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(dotColor),
    )
}

@Composable
private fun LegendPill(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
