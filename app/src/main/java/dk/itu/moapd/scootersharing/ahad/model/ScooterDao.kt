package dk.itu.moapd.scootersharing.ahad.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ScooterDao {

    @Insert
    suspend fun insert(scooter: Scooter)

    @Update
    suspend fun update(scooter: Scooter)

    @Delete
    suspend fun delete(scooter: Scooter)

    @Query("DELETE FROM scooter WHERE id=(:id)")
    fun deleteById(id: Int)

    @Query("SELECT * FROM scooter")
    fun getScooters() : Flow<List<Scooter>>

    @Query("UPDATE scooter SET isRide=(:isRide) WHERE id=(:id)")
    fun updateById(id: Int, isRide: Boolean)

    @Query("SELECT * FROM scooter WHERE id=(:id)")
    fun getScooter(id: Int) : Scooter

}