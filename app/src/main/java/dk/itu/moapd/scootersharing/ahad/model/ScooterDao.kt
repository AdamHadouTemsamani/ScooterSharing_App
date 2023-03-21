package dk.itu.moapd.scootersharing.ahad.model

import androidx.room.Dao
import androidx.room.Query
import java.util.*

@Dao
interface ScooterDao {
    @Query("SELECT * FROM scooter")
    suspend fun getScooters() : List<Scooter>

    @Query("SELECT * FROM scooter WHERE id=(:id)")
    suspend fun getScooter(id: UUID) : Scooter
}