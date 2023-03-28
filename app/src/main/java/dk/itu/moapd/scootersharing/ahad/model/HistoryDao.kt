package dk.itu.moapd.scootersharing.ahad.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface HistoryDao {

    @Insert
    suspend fun insert(historyRide: History)

    @Delete
    suspend fun delete(historyRide: History)

    @Query("SELECT * FROM scooter")
    fun getPreviousRides() : Flow<List<History>>

    @Query("SELECT * FROM scooter WHERE id=(:id)")
    fun getPreviousRide(id: Int) : History

}