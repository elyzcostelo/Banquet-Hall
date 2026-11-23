package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.NewBookingRequest
import com.example.domain.SlotAvailability
import com.example.model.DeckDate
import com.example.model.SessionBlock
import com.example.model.SetupStyle
import com.example.ui.theme.BrassGold
import com.example.ui.viewmodel.BookingSheetConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingBottomSheet(
    config: BookingSheetConfig,
    daySlots: List<SlotAvailability>,
    formError: String?,
    onDismiss: () -> Unit,
    onClearError: () -> Unit,
    onSubmit: (NewBookingRequest) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedSession by remember(config) { mutableStateOf(config.initialSession) }
    var eventTitle by remember(config) { mutableStateOf("") }
    var department by remember(config) { mutableStateOf("") }
    var organizerName by remember(config) { mutableStateOf("") }
    var contactInfo by remember(config) { mutableStateOf("") }
    var guestCountText by remember(config) { mutableStateOf("120") }
    var selectedSetup by remember(config) { mutableStateOf(SetupStyle.BANQUET_ROUNDS) }
    var specialNotes by remember(config) { mutableStateOf("") }

    val selectedSlotInfo = daySlots.firstOrNull { it.sessionBlock == selectedSession }
    val slotConflictHint = if (selectedSlotInfo != null && !selectedSlotInfo.isBookable) {
        selectedSlotInfo.conflictHint ?: "This session block is already occupied."
    } else {
        null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("booking_bottom_sheet"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    color = if (config.isMaintenanceHold) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        imageVector = if (config.isMaintenanceHold) Icons.Filled.Build else Icons.Filled.EventNote,
                        contentDescription = null,
                        tint = if (config.isMaintenanceHold) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier
                            .padding(10.dp)
                            .size(22.dp),
                    )
                }

                Column {
                    Text(
                        text = if (config.isMaintenanceHold) {
                            "Place Maintenance Hold"
                        } else {
                            "Reserve 5th Deck Hall"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${config.date.formatLongDisplay()} • June V Bldg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Session Block Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Select Session Block",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SessionBlock.entries.forEach { block ->
                        val slot = daySlots.firstOrNull { it.sessionBlock == block }
                        val available = slot?.isBookable ?: true
                        FilterChip(
                            selected = selectedSession == block,
                            onClick = {
                                selectedSession = block
                                onClearError()
                            },
                            label = {
                                Text(
                                    text = if (available) block.shortLabel else "${block.shortLabel} (Taken)",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.testTag("sheet_session_${block.name.lowercase()}"),
                        )
                    }
                }
                Text(
                    text = selectedSession.timeRangeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrassGold,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (slotConflictHint != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                ) {
                    Text(
                        text = slotConflictHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            OutlinedTextField(
                value = eventTitle,
                onValueChange = {
                    eventTitle = it
                    onClearError()
                },
                label = {
                    Text(if (config.isMaintenanceHold) "Hold Reason / Work Order" else "Event Title")
                },
                placeholder = {
                    Text(
                        if (config.isMaintenanceHold) {
                            "e.g., Floor Polishing & Lighting Calibration"
                        } else {
                            "e.g., Maritime Honors & Recognition Dinner"
                        },
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_title_input"),
            )

            if (!config.isMaintenanceHold) {
                OutlinedTextField(
                    value = department,
                    onValueChange = {
                        department = it
                        onClearError()
                    },
                    label = { Text("Department / Organization") },
                    placeholder = { Text("e.g., College of Maritime Education") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booking_department_input"),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = organizerName,
                        onValueChange = {
                            organizerName = it
                            onClearError()
                        },
                        label = { Text("Organizer Name") },
                        leadingIcon = {
                            Icon(Icons.Filled.Person, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("booking_organizer_input"),
                    )

                    OutlinedTextField(
                        value = guestCountText,
                        onValueChange = { input ->
                            guestCountText = input.filter { it.isDigit() }.take(3)
                            onClearError()
                        },
                        label = { Text("Guests") },
                        leadingIcon = {
                            Icon(Icons.Filled.Groups, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("booking_guests_input"),
                    )
                }

                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    label = { Text("Email or Mobile (Optional)") },
                    placeholder = { Text("e.g., events@aims.edu.ph") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booking_contact_input"),
                )

                // Setup Style Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Banquet Hall Setup",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SetupStyle.entries.forEach { style ->
                            FilterChip(
                                selected = selectedSetup == style,
                                onClick = {
                                    selectedSetup = style
                                    onClearError()
                                },
                                label = { Text(style.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                ),
                                modifier = Modifier.testTag("setup_chip_${style.name.lowercase()}"),
                            )
                        }
                    }
                    Text(
                        text = selectedSetup.capacitySummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            OutlinedTextField(
                value = specialNotes,
                onValueChange = { specialNotes = it },
                label = { Text("AV, Catering & Protocol Notes (Optional)") },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_notes_input"),
            )

            if (formError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = formError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize(),
                ) {
                    Text("Close")
                }

                Button(
                    onClick = {
                        onSubmit(
                            NewBookingRequest(
                                date = config.date,
                                sessionBlock = selectedSession,
                                eventTitle = eventTitle,
                                department = department,
                                organizerName = organizerName,
                                contactInfo = contactInfo,
                                guestCount = guestCountText.toIntOrNull() ?: 0,
                                setupStyle = selectedSetup,
                                specialNotes = specialNotes,
                                isMaintenanceHold = config.isMaintenanceHold,
                            ),
                        )
                    },
                    enabled = slotConflictHint == null,
                    modifier = Modifier
                        .weight(2f)
                        .minimumInteractiveComponentSize()
                        .testTag("confirm_booking_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = if (config.isMaintenanceHold) {
                            stringResource(R.string.action_place_hold)
                        } else {
                            stringResource(R.string.action_confirm_booking)
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
