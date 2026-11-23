package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BookingResult
import com.example.data.NewBookingRequest
import com.example.data.ReservationEntity
import com.example.data.ReservationRepository
import com.example.domain.ConflictEngine
import com.example.domain.DayOccupancySummary
import com.example.domain.LiveDeckStatus
import com.example.domain.SlotAvailability
import com.example.model.BookingStatus
import com.example.model.DeckDate
import com.example.model.DeckTime
import com.example.model.DeckYearMonth
import com.example.model.SessionBlock
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BookingsFilter(val label: String) {
    UPCOMING("Upcoming"),
    PAST("Past"),
    CANCELLED("Cancelled"),
}

enum class CoordinatorFilter(val label: String) {
    ALL("All"),
    CONFIRMED("Confirmed"),
    CHECKED_IN("Checked In"),
    MAINTENANCE("Holds"),
    CANCELLED("Cancelled"),
}

data class BookingSheetConfig(
    val date: DeckDate,
    val initialSession: SessionBlock,
    val isMaintenanceHold: Boolean = false,
)

data class BanquetUiState(
    val today: DeckDate = DeckDate.today(),
    val nowTime: DeckTime = DeckTime.now(),
    val displayedMonth: DeckYearMonth = DeckDate.today().toYearMonth(),
    val selectedDate: DeckDate = DeckDate.today(),
    val liveDeckStatus: LiveDeckStatus = ConflictEngine.computeLiveDeckStatus(emptyList(), DeckTime.now()),
    val monthOccupancy: Map<Int, DayOccupancySummary> = emptyMap(),
    val selectedDaySlots: List<SlotAvailability> = emptyList(),
    val selectedDayReservations: List<ReservationEntity> = emptyList(),
    val allReservations: List<ReservationEntity> = emptyList(),
    val searchQuery: String = "",
    val bookingsFilter: BookingsFilter = BookingsFilter.UPCOMING,
    val coordinatorFilter: CoordinatorFilter = CoordinatorFilter.ALL,
    val activeCount: Int = 0,
    val checkedInCount: Int = 0,
    val maintenanceHoldCount: Int = 0,
)

class BanquetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReservationRepository by lazy {
        val db = AppDatabase.getInstance(application)
        ReservationRepository(db.reservationDao())
    }

    private val _clockPulse = MutableStateFlow(DeckDate.today() to DeckTime.now())
    private val _displayedMonth = MutableStateFlow(DeckDate.today().toYearMonth())
    private val _selectedDate = MutableStateFlow(DeckDate.today())
    private val _searchQuery = MutableStateFlow("")
    private val _bookingsFilter = MutableStateFlow(BookingsFilter.UPCOMING)
    private val _coordinatorFilter = MutableStateFlow(CoordinatorFilter.ALL)

    private val _bookingSheetConfig = MutableStateFlow<BookingSheetConfig?>(null)
    val bookingSheetConfig: StateFlow<BookingSheetConfig?> = _bookingSheetConfig.asStateFlow()

    private val _confirmedPass = MutableStateFlow<ReservationEntity?>(null)
    val confirmedPass: StateFlow<ReservationEntity?> = _confirmedPass.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    val uiState: StateFlow<BanquetUiState> = combine(
        repository.allReservations,
        _clockPulse,
        _displayedMonth,
        _selectedDate,
        combine(_searchQuery, _bookingsFilter, _coordinatorFilter) { q, bf, cf ->
            Triple(q, bf, cf)
        },
    ) { reservations, (today, nowTime), displayedMonth, selectedDate, (query, bFilter, cFilter) ->
        val byDate = reservations.groupBy { it.dateIso }
        val todayReservations = byDate[today.toIsoString()].orEmpty()
        val selectedReservations = byDate[selectedDate.toIsoString()].orEmpty()

        val daysInMonth = displayedMonth.daysInMonth()
        val monthMap = (1..daysInMonth).associateWith { day ->
            val date = displayedMonth.atDay(day)
            ConflictEngine.evaluateDayOccupancy(
                date = date,
                reservationsForDate = byDate[date.toIsoString()].orEmpty(),
                today = today,
            )
        }

        val liveStatus = ConflictEngine.computeLiveDeckStatus(todayReservations, nowTime)
        val slotStates = ConflictEngine.evaluateSlotStates(
            date = selectedDate,
            reservationsForDate = selectedReservations,
            today = today,
            nowTime = nowTime,
        )

        BanquetUiState(
            today = today,
            nowTime = nowTime,
            displayedMonth = displayedMonth,
            selectedDate = selectedDate,
            liveDeckStatus = liveStatus,
            monthOccupancy = monthMap,
            selectedDaySlots = slotStates,
            selectedDayReservations = selectedReservations,
            allReservations = reservations,
            searchQuery = query,
            bookingsFilter = bFilter,
            coordinatorFilter = cFilter,
            activeCount = reservations.count {
                it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.CHECKED_IN
            },
            checkedInCount = reservations.count { it.status == BookingStatus.CHECKED_IN },
            maintenanceHoldCount = reservations.count { it.status == BookingStatus.MAINTENANCE_HOLD },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BanquetUiState(),
    )

    init {
        viewModelScope.launch {
            repository.seedDemoDataIfEmpty()
        }
        viewModelScope.launch {
            while (true) {
                delay(15_000L)
                _clockPulse.value = DeckDate.today() to DeckTime.now()
            }
        }
    }

    fun selectDate(date: DeckDate) {
        _selectedDate.value = date
        _displayedMonth.value = date.toYearMonth()
    }

    fun previousMonth() {
        _displayedMonth.value = _displayedMonth.value.plusMonths(-1)
    }

    fun nextMonth() {
        _displayedMonth.value = _displayedMonth.value.plusMonths(1)
    }

    fun jumpToToday() {
        val today = DeckDate.today()
        _selectedDate.value = today
        _displayedMonth.value = today.toYearMonth()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setBookingsFilter(filter: BookingsFilter) {
        _bookingsFilter.value = filter
    }

    fun setCoordinatorFilter(filter: CoordinatorFilter) {
        _coordinatorFilter.value = filter
    }

    fun openBookingSheet(
        date: DeckDate = _selectedDate.value,
        session: SessionBlock = SessionBlock.MORNING,
        isMaintenanceHold: Boolean = false,
    ) {
        _selectedDate.value = date
        _displayedMonth.value = date.toYearMonth()
        _formError.value = null
        _bookingSheetConfig.value = BookingSheetConfig(
            date = date,
            initialSession = session,
            isMaintenanceHold = isMaintenanceHold,
        )
    }

    fun closeBookingSheet() {
        _formError.value = null
        _bookingSheetConfig.value = null
    }

    fun clearFormError() {
        _formError.value = null
    }

    fun submitBooking(request: NewBookingRequest) {
        viewModelScope.launch {
            when (val result = repository.createReservation(request, DeckDate.today())) {
                is BookingResult.Success -> {
                    _bookingSheetConfig.value = null
                    _formError.value = null
                    _selectedDate.value = request.date
                    _displayedMonth.value = request.date.toYearMonth()
                    if (request.isMaintenanceHold) {
                        _feedbackMessage.value = "Maintenance hold placed on ${request.date.formatShortMonthDay()}"
                    } else {
                        _confirmedPass.value = result.reservation
                    }
                }
                is BookingResult.Conflict -> _formError.value = result.message
                is BookingResult.ValidationError -> _formError.value = result.message
            }
        }
    }

    fun dismissConfirmedPass() {
        _confirmedPass.value = null
    }

    fun updateReservationStatus(reservation: ReservationEntity, newStatus: BookingStatus) {
        viewModelScope.launch {
            repository.updateStatus(reservation.id, newStatus)
            _feedbackMessage.value = when (newStatus) {
                BookingStatus.CHECKED_IN -> "${reservation.referenceCode} checked in at 5th Deck"
                BookingStatus.CANCELLED -> "${reservation.referenceCode} cancelled — slot reopened"
                BookingStatus.CONFIRMED -> "${reservation.referenceCode} restored to Confirmed"
                BookingStatus.MAINTENANCE_HOLD -> "${reservation.referenceCode} set to Maintenance Hold"
            }
        }
    }

    fun consumeFeedbackMessage() {
        _feedbackMessage.value = null
    }
}
