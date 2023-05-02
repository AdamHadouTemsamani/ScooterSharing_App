package dk.itu.moapd.scootersharing.ahad.application

import android.app.Application
import dk.itu.moapd.scootersharing.ahad.database.ScooterDatabase
import dk.itu.moapd.scootersharing.ahad.model.HistoryRepository
import dk.itu.moapd.scootersharing.ahad.model.ScooterRepository
import dk.itu.moapd.scootersharing.ahad.model.UserBalanceRepository

inline fun <reified T> T.TAG(): String = T::class.java.simpleName
class ScooterApplication : Application() {

    private val database by lazy {
        ScooterDatabase.getDatabase(this)
    }

    val scooterRepository by lazy {
        ScooterRepository(database.scooterDao())
    }

    val historyRepository by lazy {
        HistoryRepository(database.historyDao())
    }

    val userRepository by lazy {
        UserBalanceRepository(database.userDao())
    }
}