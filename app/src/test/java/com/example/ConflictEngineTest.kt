package com.example

import com.example.data.ReservationEntity
import com.example.domain.ConflictEngine
import com.example.domain.DayOccupancyState
import com.example.domain.LiveHallCondition
import com.example.domain.SlotStatus
import com.example.model.BookingStatus
import com.example.model.DeckDate
import com.example.model.DeckTime
import com.example.model.SessionBlock
import com.example.model.SetupStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConflictEngineTest {

    private val today = DeckDate(2026, 11, 23)
    private val tomorrow = DeckDate(2026, 11, 24)

    private fun sampleReservation(
        date: DeckDate = tomorrow,
        block: SessionBlock = SessionBlock.MORNING,
        status: BookingStatus = BookingStatus.CONFIRMED,
    ) = ReservationEntity(
        id = 1L,
        referenceCode = "AIMS-5D-1001",
        dateIso = date.toIsoString(),
        sessionBlock = block,
        eventTitle = "Maritime Leadership Forum",
        department = "College of Maritime Education",
        organizerName = "Capt. Reyes",
        contactInfo = "events@aims.edu.ph",
        guestCount = 140,
        setupStyle = SetupStyle.BANQUET_ROUNDS,
        status = status,
    )

    @Test
    fun `full day conflicts with morning afternoon and evening`() {
        val existing = listOf(sampleReservation(block = SessionBlock.MORNING))
        assertNotNull(ConflictEngine.findConflictingReservation(SessionBlock.FULL_DAY, existing))
        assertNull(ConflictEngine.findConflictingReservation(SessionBlock.AFTERNOON, existing))
        assertNull(ConflictEngine.findConflictingReservation(SessionBlock.EVENING, existing))
    }

    @Test
    fun `cancelled reservation does not block slots`() {
        val existing = listOf(
            sampleReservation(block = SessionBlock.FULL_DAY, status = BookingStatus.CANCELLED),
        )
        assertNull(ConflictEngine.findConflictingReservation(SessionBlock.MORNING, existing))
        val summary = ConflictEngine.evaluateDayOccupancy(tomorrow, existing, today)
        assertEquals(DayOccupancyState.OPEN, summary.state)
        assertFalse(summary.morningReserved)
    }

    @Test
    fun `full day booking blocks all partial sessions on slot timeline`() {
        val existing = listOf(sampleReservation(block = SessionBlock.FULL_DAY))
        val slots = ConflictEngine.evaluateSlotStates(tomorrow, existing, today, DeckTime(9, 0))

        val morning = slots.first { it.sessionBlock == SessionBlock.MORNING }
        val fullDay = slots.first { it.sessionBlock == SessionBlock.FULL_DAY }

        assertEquals(SlotStatus.BLOCKED_BY_FULL_DAY, morning.status)
        assertEquals(SlotStatus.RESERVED, fullDay.status)
        assertTrue(slots.none { it.isBookable })
    }

    @Test
    fun `live deck status reflects active session in real time`() {
        val todayMorning = sampleReservation(date = today, block = SessionBlock.MORNING)
        val duringMorning = ConflictEngine.computeLiveDeckStatus(listOf(todayMorning), DeckTime(10, 15))
        assertEquals(LiveHallCondition.OCCUPIED_NOW, duringMorning.condition)

        val duringAfternoon = ConflictEngine.computeLiveDeckStatus(listOf(todayMorning), DeckTime(14, 0))
        assertEquals(LiveHallCondition.OPEN_NOW, duringAfternoon.condition)
    }
}
