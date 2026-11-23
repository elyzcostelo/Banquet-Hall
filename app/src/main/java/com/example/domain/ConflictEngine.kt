package com.example.domain

import com.example.data.ReservationEntity
import com.example.model.BookingStatus
import com.example.model.DeckDate
import com.example.model.DeckTime
import com.example.model.SessionBlock

enum class DayOccupancyState {
    OPEN,
    PARTIAL,
    FULL,
    MAINTENANCE,
    PAST,
}

data class DayOccupancySummary(
    val date: DeckDate,
    val state: DayOccupancyState,
    val morningReserved: Boolean,
    val afternoonReserved: Boolean,
    val eveningReserved: Boolean,
    val hasMaintenanceHold: Boolean,
    val activeReservationCount: Int,
)

enum class SlotStatus {
    AVAILABLE,
    RESERVED,
    LIVE_IN_PROGRESS,
    MAINTENANCE_HOLD,
    BLOCKED_BY_FULL_DAY,
    BLOCKED_BY_PARTIAL,
    PAST_DATE_OR_TIME,
}

data class SlotAvailability(
    val sessionBlock: SessionBlock,
    val status: SlotStatus,
    val blockingReservation: ReservationEntity? = null,
    val conflictHint: String? = null,
) {
    val isBookable: Boolean
        get() = status == SlotStatus.AVAILABLE
}

enum class LiveHallCondition {
    OCCUPIED_NOW,
    MAINTENANCE_NOW,
    OPEN_NOW,
    AFTER_HOURS,
}

data class LiveDeckStatus(
    val condition: LiveHallCondition,
    val currentTimeFormatted: String,
    val headline: String,
    val detail: String,
    val activeReservation: ReservationEntity? = null,
)

object ConflictEngine {

    fun findConflictingReservation(
        targetBlock: SessionBlock,
        existingForDate: List<ReservationEntity>,
    ): ReservationEntity? {
        return existingForDate
            .filter { it.status.isBlocking }
            .firstOrNull { it.sessionBlock.conflictsWith(targetBlock) }
    }

    fun evaluateDayOccupancy(
        date: DeckDate,
        reservationsForDate: List<ReservationEntity>,
        today: DeckDate,
    ): DayOccupancySummary {
        val active = reservationsForDate.filter { it.status.isBlocking }
        val hasFullDay = active.any { it.sessionBlock == SessionBlock.FULL_DAY }
        val morning = hasFullDay || active.any { it.sessionBlock == SessionBlock.MORNING }
        val afternoon = hasFullDay || active.any { it.sessionBlock == SessionBlock.AFTERNOON }
        val evening = hasFullDay || active.any { it.sessionBlock == SessionBlock.EVENING }
        val hasHold = active.any { it.status == BookingStatus.MAINTENANCE_HOLD }

        val state = when {
            date < today -> DayOccupancyState.PAST
            hasHold && morning && afternoon && evening -> DayOccupancyState.MAINTENANCE
            morning && afternoon && evening -> DayOccupancyState.FULL
            hasHold && active.size == 1 -> DayOccupancyState.MAINTENANCE
            active.isNotEmpty() -> DayOccupancyState.PARTIAL
            else -> DayOccupancyState.OPEN
        }

        return DayOccupancySummary(
            date = date,
            state = state,
            morningReserved = morning,
            afternoonReserved = afternoon,
            eveningReserved = evening,
            hasMaintenanceHold = hasHold,
            activeReservationCount = active.size,
        )
    }

    fun evaluateSlotStates(
        date: DeckDate,
        reservationsForDate: List<ReservationEntity>,
        today: DeckDate,
        nowTime: DeckTime,
    ): List<SlotAvailability> {
        val active = reservationsForDate.filter { it.status.isBlocking }
        val isPastDate = date < today
        val isToday = date == today

        val fullDayReservation = active.firstOrNull { it.sessionBlock == SessionBlock.FULL_DAY }
        val partialReservations = active.filter { it.sessionBlock != SessionBlock.FULL_DAY }

        return SessionBlock.entries.map { block ->
            val directReservation = active.firstOrNull { it.sessionBlock == block }

            when {
                directReservation != null -> {
                    val isLiveNow = isToday &&
                        nowTime.hour in block.startHour until block.endHour
                    val slotStatus = when {
                        directReservation.status == BookingStatus.MAINTENANCE_HOLD ->
                            SlotStatus.MAINTENANCE_HOLD
                        isLiveNow -> SlotStatus.LIVE_IN_PROGRESS
                        else -> SlotStatus.RESERVED
                    }
                    SlotAvailability(
                        sessionBlock = block,
                        status = slotStatus,
                        blockingReservation = directReservation,
                    )
                }

                block != SessionBlock.FULL_DAY && fullDayReservation != null -> {
                    SlotAvailability(
                        sessionBlock = block,
                        status = SlotStatus.BLOCKED_BY_FULL_DAY,
                        blockingReservation = fullDayReservation,
                        conflictHint = "Covered by Full Day Charter (${fullDayReservation.eventTitle})",
                    )
                }

                block == SessionBlock.FULL_DAY && partialReservations.isNotEmpty() -> {
                    val labels = partialReservations.joinToString(", ") { it.sessionBlock.shortLabel }
                    SlotAvailability(
                        sessionBlock = block,
                        status = SlotStatus.BLOCKED_BY_PARTIAL,
                        blockingReservation = partialReservations.first(),
                        conflictHint = "Unavailable — $labels already reserved",
                    )
                }

                isPastDate -> {
                    SlotAvailability(
                        sessionBlock = block,
                        status = SlotStatus.PAST_DATE_OR_TIME,
                        conflictHint = "Past date",
                    )
                }

                isToday && nowTime.hour >= block.endHour -> {
                    SlotAvailability(
                        sessionBlock = block,
                        status = SlotStatus.PAST_DATE_OR_TIME,
                        conflictHint = "Session window has ended for today",
                    )
                }

                isToday && block == SessionBlock.FULL_DAY && nowTime.hour >= SessionBlock.MORNING.endHour -> {
                    SlotAvailability(
                        sessionBlock = block,
                        status = SlotStatus.PAST_DATE_OR_TIME,
                        conflictHint = "Full Day window starts at 08:00 AM",
                    )
                }

                else -> SlotAvailability(
                    sessionBlock = block,
                    status = SlotStatus.AVAILABLE,
                )
            }
        }
    }

    fun computeLiveDeckStatus(
        todayReservations: List<ReservationEntity>,
        nowTime: DeckTime,
    ): LiveDeckStatus {
        val activeToday = todayReservations.filter { it.status.isBlocking }
        val timeText = nowTime.format12Hour()

        if (nowTime.hour < 8 || nowTime.hour >= 22) {
            return LiveDeckStatus(
                condition = LiveHallCondition.AFTER_HOURS,
                currentTimeFormatted = timeText,
                headline = "5th Deck Secured • After Hours",
                detail = if (nowTime.hour < 8) {
                    "Operations open at 08:00 AM today"
                } else {
                    "Hall operations resume at 08:00 AM tomorrow"
                },
            )
        }

        val currentBlock = when (nowTime.hour) {
            in 8 until 12 -> SessionBlock.MORNING
            in 13 until 17 -> SessionBlock.AFTERNOON
            in 18 until 22 -> SessionBlock.EVENING
            else -> null // 12:00-13:00 or 17:00-18:00 turnover buffer
        }

        val fullDay = activeToday.firstOrNull { it.sessionBlock == SessionBlock.FULL_DAY }
        val currentMatch = fullDay ?: currentBlock?.let { blk ->
            activeToday.firstOrNull { it.sessionBlock == blk }
        }

        if (currentMatch != null) {
            val isHold = currentMatch.status == BookingStatus.MAINTENANCE_HOLD
            return LiveDeckStatus(
                condition = if (isHold) LiveHallCondition.MAINTENANCE_NOW else LiveHallCondition.OCCUPIED_NOW,
                currentTimeFormatted = timeText,
                headline = if (isHold) {
                    "Maintenance Hold In Effect"
                } else {
                    "On Deck Now: ${currentMatch.eventTitle}"
                },
                detail = "${currentMatch.sessionBlock.displayName} • Until ${formatEndHour(currentMatch.sessionBlock.endHour)}",
                activeReservation = currentMatch,
            )
        }

        val nextReservation = activeToday
            .filter { it.sessionBlock.startHour > nowTime.hour }
            .minByOrNull { it.sessionBlock.startHour }

        val turnoverNote = if (currentBlock == null) {
            "Deck turnover window in progress"
        } else {
            "${currentBlock.displayName} is currently open"
        }

        val nextNote = if (nextReservation != null) {
            "Next: ${nextReservation.eventTitle} at ${formatStartHour(nextReservation.sessionBlock.startHour)}"
        } else {
            "No remaining bookings scheduled today"
        }

        return LiveDeckStatus(
            condition = LiveHallCondition.OPEN_NOW,
            currentTimeFormatted = timeText,
            headline = "5th Deck Available Now",
            detail = "$turnoverNote • $nextNote",
        )
    }

    private fun formatStartHour(hour: Int): String = DeckTime(hour, 0).format12Hour()
    private fun formatEndHour(hour: Int): String = DeckTime(hour, 0).format12Hour()
}
