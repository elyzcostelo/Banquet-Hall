package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.BookingStatus
import com.example.model.SessionBlock
import com.example.model.SetupStyle

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referenceCode: String,
    val dateIso: String,
    val sessionBlock: SessionBlock,
    val eventTitle: String,
    val department: String,
    val organizerName: String,
    val contactInfo: String,
    val guestCount: Int,
    val setupStyle: SetupStyle,
    val specialNotes: String = "",
    val status: BookingStatus = BookingStatus.CONFIRMED,
    val createdAtMillis: Long = System.currentTimeMillis(),
)
