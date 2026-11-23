package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.ReservationEntity
import com.example.model.DeckDate
import com.example.ui.theme.BrassGold

@Composable
fun BookingPassDialog(
    reservation: ReservationEntity,
    onDismiss: () -> Unit,
) {
    val formattedDate = DeckDate.parseIso(reservation.dateIso)?.formatFullDisplay()
        ?: reservation.dateIso

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("booking_pass_dialog"),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, BrassGold),
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = null,
                        tint = BrassGold,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(32.dp),
                    )
                }

                Text(
                    text = "Reservation Confirmed",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = reservation.referenceCode,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }

                Text(
                    text = "AIMS • June V Building • 5th Deck Banquet Hall",
                    style = MaterialTheme.typography.labelMedium,
                    color = BrassGold,
                    textAlign = TextAlign.Center,
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                PassDetailRow("Event", reservation.eventTitle)
                PassDetailRow("Date", formattedDate)
                PassDetailRow(
                    "Session",
                    "${reservation.sessionBlock.displayName} (${reservation.sessionBlock.timeRangeLabel})",
                )
                PassDetailRow("Organizer", "${reservation.organizerName} • ${reservation.department}")
                PassDetailRow("Setup", "${reservation.setupStyle.displayName} (${reservation.guestCount} guests)")

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("pass_done_button"),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(text = stringResource(R.string.action_close))
                }
            }
        }
    }
}

@Composable
private fun PassDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.32f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.68f),
        )
    }
}
