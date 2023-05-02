package dk.itu.moapd.scootersharing.ahad.model

import android.content.Context
import androidx.room.Room
import dk.itu.moapd.scootersharing.ahad.database.ScooterDatabase
import java.util.*
import kotlinx.coroutines.flow.Flow
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData

private const val DATABASE_NAME = "scooter_database"

class ScooterRepository(private val scooterDao: ScooterDao) {

    val scooters: Flow<List<Scooter>> = scooterDao.getScooters()

    @WorkerThread
    suspend fun insert(scooter: Scooter) {
        scooterDao.insert(scooter)
    }

    @WorkerThread
    suspend fun update(scooter: Scooter) {
        scooterDao.update(scooter)
    }

    @WorkerThread
    suspend fun delete(scooter: Scooter) {
        scooterDao.delete(scooter)
    }
    

}