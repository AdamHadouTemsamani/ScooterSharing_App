package dk.itu.moapd.scootersharing.ahad.model

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {

    val previousRides : Flow<List<History>> = historyDao.getPreviousRides()

    @WorkerThread
    suspend fun insert(previousRide: History) {
        historyDao.insert(previousRide)
    }

    @WorkerThread
    suspend fun delete(previousRide: History) {
        historyDao.delete(previousRide)
    }
}