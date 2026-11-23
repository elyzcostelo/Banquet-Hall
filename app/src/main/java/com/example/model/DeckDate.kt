package com.example.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Pure Kotlin / java.util.Calendar-backed date model supporting API 24+ natively
 * without requiring core library desugaring.
 */
data class DeckDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int,   // 1..31
) : Comparable<DeckDate> {

    fun toIsoString(): String =
        String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

    fun toYearMonth(): DeckYearMonth = DeckYearMonth(year, month)

    fun toCalendar(): Calendar = Calendar.getInstance().apply {
        clear()
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, day)
    }

    fun plusDays(days: Int): DeckDate {
        val cal = toCalendar()
        cal.add(Calendar.DAY_OF_MONTH, days)
        return fromCalendar(cal)
    }

    fun formatFullDisplay(): String {
        val formatter = SimpleDateFormat("EEE, MMM d, yyyy", Locale.US)
        return formatter.format(toCalendar().time)
    }

    fun formatLongDisplay(): String {
        val formatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
        return formatter.format(toCalendar().time)
    }

    fun formatShortMonthDay(): String {
        val formatter = SimpleDateFormat("MMM d", Locale.US)
        return formatter.format(toCalendar().time)
    }

    override fun compareTo(other: DeckDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    companion object {
        fun today(): DeckDate = fromCalendar(Calendar.getInstance())

        fun fromCalendar(cal: Calendar): DeckDate = DeckDate(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1,
            day = cal.get(Calendar.DAY_OF_MONTH),
        )

        fun parseIso(iso: String): DeckDate? {
            val parts = iso.split("-")
            if (parts.size != 3) return null
            val y = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            val d = parts[2].toIntOrNull() ?: return null
            return DeckDate(y, m, d)
        }
    }
}

data class DeckYearMonth(
    val year: Int,
    val month: Int, // 1..12
) : Comparable<DeckYearMonth> {

    fun plusMonths(delta: Int): DeckYearMonth {
        val cal = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, delta)
        }
        return DeckYearMonth(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1,
        )
    }

    fun daysInMonth(): Int {
        val cal = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /** Returns 0 = Sunday, 1 = Monday, ..., 6 = Saturday. */
    fun firstDayOfWeekZeroBased(): Int {
        val cal = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
    }

    fun atDay(day: Int): DeckDate = DeckDate(year, month, day)

    fun formatMonthYear(): String {
        val cal = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)
    }

    override fun compareTo(other: DeckYearMonth): Int {
        if (year != other.year) return year.compareTo(other.year)
        return month.compareTo(other.month)
    }
}

data class DeckTime(
    val hour: Int,   // 0..23
    val minute: Int, // 0..59
) : Comparable<DeckTime> {

    fun totalMinutes(): Int = hour * 60 + minute

    fun format12Hour(): String {
        val period = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.US, "%d:%02d %s", displayHour, minute, period)
    }

    override fun compareTo(other: DeckTime): Int =
        totalMinutes().compareTo(other.totalMinutes())

    companion object {
        fun now(): DeckTime {
            val cal = Calendar.getInstance()
            return DeckTime(
                hour = cal.get(Calendar.HOUR_OF_DAY),
                minute = cal.get(Calendar.MINUTE),
            )
        }
    }
}
