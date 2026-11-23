package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.BookingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations ORDER BY dateIso ASC, createdAtMillis DESC")
    fun observeAllReservations(): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE dateIso = :dateIso AND status != 'CANCELLED'")
    suspend fun getBlockingReservationsForDate(dateIso: String): List<ReservationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(entity: ReservationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ReservationEntity>)

    @Query("UPDATE reservations SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: BookingStatus)

    @Query("SELECT COUNT(*) FROM reservations")
    suspend fun countReservations(): Int
}
