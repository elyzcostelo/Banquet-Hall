package com.example.model

enum class SessionBlock(
    val displayName: String,
    val shortLabel: String,
    val timeRangeLabel: String,
    val startHour: Int,
    val endHour: Int,
) {
    MORNING(
        displayName = "Morning Watch",
        shortLabel = "Morning",
        timeRangeLabel = "08:00 AM – 12:00 PM",
        startHour = 8,
        endHour = 12,
    ),
    AFTERNOON(
        displayName = "Afternoon Session",
        shortLabel = "Afternoon",
        timeRangeLabel = "01:00 PM – 05:00 PM",
        startHour = 13,
        endHour = 17,
    ),
    EVENING(
        displayName = "Evening Gala",
        shortLabel = "Evening",
        timeRangeLabel = "06:00 PM – 10:00 PM",
        startHour = 18,
        endHour = 22,
    ),
    FULL_DAY(
        displayName = "Full Day Charter",
        shortLabel = "Full Day",
        timeRangeLabel = "08:00 AM – 10:00 PM",
        startHour = 8,
        endHour = 22,
    );

    fun conflictsWith(other: SessionBlock): Boolean {
        if (this == other) return true
        return this == FULL_DAY || other == FULL_DAY
    }
}

enum class SetupStyle(
    val displayName: String,
    val capacitySummary: String,
    val maxCapacity: Int,
) {
    BANQUET_ROUNDS(
        displayName = "Banquet Rounds",
        capacitySummary = "Round tables of 8–10 • up to 280 guests",
        maxCapacity = 280,
    ),
    THEATER(
        displayName = "Theater Seating",
        capacitySummary = "Forward stage rows • up to 350 guests",
        maxCapacity = 350,
    ),
    CEREMONIAL(
        displayName = "Maritime Ceremonial",
        capacitySummary = "Center parade aisle & podium • up to 300 guests",
        maxCapacity = 300,
    ),
    CONFERENCE_U(
        displayName = "Executive U-Shape",
        capacitySummary = "Board & plenary tables • up to 120 guests",
        maxCapacity = 120,
    ),
}

enum class BookingStatus(val displayName: String) {
    CONFIRMED("Confirmed"),
    CHECKED_IN("Checked In"),
    MAINTENANCE_HOLD("Maintenance Hold"),
    CANCELLED("Cancelled");

    val isBlocking: Boolean
        get() = this != CANCELLED
}

object VenueSpecs {
    const val INSTITUTION_NAME = "Asian Institute of Maritime Studies (AIMS)"
    const val BUILDING_DECK = "June V Building • 5th Deck Banquet Hall"
    const val MAX_HALL_CAPACITY = 350
}
