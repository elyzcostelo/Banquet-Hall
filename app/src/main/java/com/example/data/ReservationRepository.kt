package com.example.data

import com.example.domain.ConflictEngine
import com.example.model.BookingStatus
import com.example.model.DeckDate
import com.example.model.SessionBlock
import com.example.model.SetupStyle
import com.example.model.VenueSpecs
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

sealed interface BookingResult {
    data class Success(val reservation: ReservationEntity) : BookingResult
    data class Conflict(val message: String) : BookingResult
    data class ValidationError(val message: String) : BookingResult
}

data class NewBookingRequest(
    val date: DeckDate,
    val sessionBlock: SessionBlock,
    val eventTitle: String,
    val department: String,
    val organizerName: String,
    val contactInfo: String,
    val guestCount: Int,
    val setupStyle: SetupStyle,
    val specialNotes: String,
    val isMaintenanceHold: Boolean = false,
)

class ReservationRepository(private val dao: ReservationDao) {

    val allReservations: Flow<List<ReservationEntity>> = dao.observeAllReservations()

    suspend fun createReservation(
        request: NewBookingRequest,
        today: DeckDate = DeckDate.today(),
    ): BookingResult {
        if (request.date < today) {
            return BookingResult.ValidationError("Cannot reserve a past date.")
        }
        val title = request.eventTitle.trim()
        if (title.isEmpty()) {
            return BookingResult.ValidationError("Please enter an event or hold title.")
        }
        if (!request.isMaintenanceHold) {
            if (request.organizerName.trim().isEmpty()) {
                return BookingResult.ValidationError("Please provide the organizer's name.")
            }
            if (request.department.trim().isEmpty()) {
                return BookingResult.ValidationError("Please specify a department or organization.")
            }
            if (request.guestCount < 1 || request.guestCount > VenueSpecs.MAX_HALL_CAPACITY) {
                return BookingResult.ValidationError(
                    "Guest count must be between 1 and ${VenueSpecs.MAX_HALL_CAPACITY}."
                )
            }
            if (request.guestCount > request.setupStyle.maxCapacity) {
                return BookingResult.ValidationError(
                    "${request.setupStyle.displayName} supports up to ${request.setupStyle.maxCapacity} guests."
                )
            }
        }

        val dateIso = request.date.toIsoString()
        val existing = dao.getBlockingReservationsForDate(dateIso)
        val conflict = ConflictEngine.findConflictingReservation(request.sessionBlock, existing)
        if (conflict != null) {
            return BookingResult.Conflict(
                "Slot conflict with \"${conflict.eventTitle}\" (${conflict.sessionBlock.displayName})."
            )
        }

        val refPrefix = if (request.isMaintenanceHold) "AIMS-HOLD" else "AIMS-5D"
        val refNum = Random.nextInt(1000, 9999)
        val entity = ReservationEntity(
            referenceCode = "$refPrefix-$refNum",
            dateIso = dateIso,
            sessionBlock = request.sessionBlock,
            eventTitle = title,
            department = if (request.isMaintenanceHold) {
                "June V Building Facilities"
            } else {
                request.department.trim()
            },
            organizerName = if (request.isMaintenanceHold) {
                "5th Deck Coordinator"
            } else {
                request.organizerName.trim()
            },
            contactInfo = request.contactInfo.trim().ifEmpty { "AIMS Internal Desk" },
            guestCount = if (request.isMaintenanceHold) 0 else request.guestCount,
            setupStyle = request.setupStyle,
            specialNotes = request.specialNotes.trim(),
            status = if (request.isMaintenanceHold) {
                BookingStatus.MAINTENANCE_HOLD
            } else {
                BookingStatus.CONFIRMED
            },
        )
        val insertedId = dao.insertReservation(entity)
        return BookingResult.Success(entity.copy(id = insertedId))
    }

    suspend fun updateStatus(id: Long, status: BookingStatus) {
        dao.updateStatus(id, status)
    }

    suspend fun seedDemoDataIfEmpty(today: DeckDate = DeckDate.today()) {
        if (dao.countReservations() > 0) return

        val sampleBookings = listOf(
            ReservationEntity(
                referenceCode = "AIMS-5D-2041",
                dateIso = today.toIsoString(),
                sessionBlock = SessionBlock.EVENING,
                eventTitle = "Deck Officers' Leadership Colloquium",
                department = "School of Maritime Transport",
                organizerName = "Capt. Rafael Mendoza",
                contactInfo = "r.mendoza@aims.edu.ph",
                guestCount = 160,
                setupStyle = SetupStyle.BANQUET_ROUNDS,
                specialNotes = "Podium mic, Philippine & AIMS colors on stage.",
                status = BookingStatus.CONFIRMED,
            ),
            ReservationEntity(
                referenceCode = "AIMS-5D-2048",
                dateIso = today.plusDays(1).toIsoString(),
                sessionBlock = SessionBlock.MORNING,
                eventTitle = "Marine Engineering Alumni Breakfast",
                department = "AIMS Alumni Association",
                organizerName = "Engr. Clarissa Santos",
                contactInfo = "alumni@aims.edu.ph",
                guestCount = 110,
                setupStyle = SetupStyle.BANQUET_ROUNDS,
                specialNotes = "Projector & wireless lavalier mic requested.",
                status = BookingStatus.CONFIRMED,
            ),
            ReservationEntity(
                referenceCode = "AIMS-5D-2053",
                dateIso = today.plusDays(3).toIsoString(),
                sessionBlock = SessionBlock.FULL_DAY,
                eventTitle = "Annual Maritime Cadet Commissioning Gala",
                department = "Office of Student Affairs",
                organizerName = "Cmdr. Eduardo Velasco",
                contactInfo = "osa@aims.edu.ph",
                guestCount = 290,
                setupStyle = SetupStyle.CEREMONIAL,
                specialNotes = "Full ceremonial aisle, brass stanchions & dual LED screens.",
                status = BookingStatus.CONFIRMED,
            ),
            ReservationEntity(
                referenceCode = "AIMS-HOLD-109",
                dateIso = today.plusDays(5).toIsoString(),
                sessionBlock = SessionBlock.AFTERNOON,
                eventTitle = "5th Deck HVAC & Acoustic Rigging Inspection",
                department = "June V Building Facilities",
                organizerName = "5th Deck Coordinator",
                contactInfo = "facilities@aims.edu.ph",
                guestCount = 0,
                setupStyle = SetupStyle.THEATER,
                specialNotes = "Scheduled preventive maintenance.",
                status = BookingStatus.MAINTENANCE_HOLD,
            ),
        )
        dao.insertAll(sampleBookings)
    }
}
