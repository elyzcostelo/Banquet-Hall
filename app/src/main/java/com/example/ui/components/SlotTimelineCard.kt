package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.SlotAvailability
import com.example.domain.SlotStatus
import com.example.ui.theme.BrassGold
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusCrimson
import com.example.ui.theme.StatusEmerald

@Composable
fun SlotTimelineCard(
    slot: SlotAvailability,
    onReserveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAvailable = slot.isBookable
    val isLive = slot.status == SlotStatus.LIVE_IN_PROGRESS
    val isHold = slot.status == SlotStatus.MAINTENANCE_HOLD

    val statusColor: Color
    val statusBadgeText: String
    val statusIcon = when (slot.status) {
        SlotStatus.AVAILABLE -> {
            statusColor = StatusEmerald
            statusBadgeText = "Available"
            Icons.Filled.EventAvailable
        }
        SlotStatus.LIVE_IN_PROGRESS -> {
            statusColor = StatusCrimson
            statusBadgeText = "In Progress"
            Icons.Filled.RadioButtonChecked
        }
        SlotStatus.RESERVED -> {
            statusColor = StatusCrimson
            statusBadgeText = "Reserved"
            Icons.Filled.CheckCircle
        }
        SlotStatus.MAINTENANCE_HOLD -> {
            statusColor = StatusAmber
            statusBadgeText = "Hold"
            Icons.Filled.Build
        }
        SlotStatus.BLOCKED_BY_FULL_DAY,
        SlotStatus.BLOCKED_BY_PARTIAL,
        SlotStatus.PAST_DATE_OR_TIME -> {
            statusColor = MaterialTheme.colorScheme.onSurfaceVariant
            statusBadgeText = if (slot.status == SlotStatus.PAST_DATE_OR_TIME) "Closed" else "Unavailable"
            Icons.Filled.Block
        }
    }

    val cardBorder = when {
        isLive -> BorderStroke(1.5.dp, StatusCrimson)
        isAvailable -> BorderStroke(1.dp, BrassGold.copy(alpha = 0.55f))
        isHold -> BorderStroke(1.dp, StatusAmber.copy(alpha = 0.6f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("slot_card_${slot.sessionBlock.name.lowercase()}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isAvailable) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            },
        ),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isAvailable) 2.dp else 0.dp),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slot.sessionBlock.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = slot.sessionBlock.timeRangeLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Surface(
                    color = statusColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusBadgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            val booking = slot.blockingReservation
            if (isAvailable) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Open for instant booking • Up to 350 guests",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onReserveClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("reserve_slot_${slot.sessionBlock.name.lowercase()}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.action_reserve_slot),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            } else if (slot.conflictHint != null) {
                Text(
                    text = slot.conflictHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (booking != null) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = booking.eventTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${booking.department} • ${booking.organizerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (booking.guestCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Groups,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = BrassGold,
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${booking.guestCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    Text(
                        text = "${booking.referenceCode} • ${booking.setupStyle.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrassGold,
                    )
                }
            }
        }
    }
}
