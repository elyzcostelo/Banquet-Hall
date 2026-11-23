package com.example.data

import androidx.room.TypeConverter
import com.example.model.BookingStatus
import com.example.model.SessionBlock
import com.example.model.SetupStyle

class RoomConverters {
    @TypeConverter
    fun fromSessionBlock(value: SessionBlock): String = value.name

    @TypeConverter
    fun toSessionBlock(value: String): SessionBlock = SessionBlock.valueOf(value)

    @TypeConverter
    fun fromSetupStyle(value: SetupStyle): String = value.name

    @TypeConverter
    fun toSetupStyle(value: String): SetupStyle = SetupStyle.valueOf(value)

    @TypeConverter
    fun fromBookingStatus(value: BookingStatus): String = value.name

    @TypeConverter
    fun toBookingStatus(value: String): BookingStatus = BookingStatus.valueOf(value)
}
