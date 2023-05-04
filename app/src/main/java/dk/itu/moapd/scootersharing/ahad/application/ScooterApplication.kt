package dk.itu.moapd.scootersharing.ahad.application

import android.app.Application
import dk.itu.moapd.scootersharing.ahad.database.ScooterDatabase
import dk.itu.moapd.scootersharing.ahad.model.HistoryRepository
import dk.itu.moapd.scootersharing.ahad.model.ScooterRepository
import dk.itu.moapd.scootersharing.ahad.model.UserBalanceRepository

inline fun <reified T> T.TAG(): String = T::class.java.simpleName
class ScooterApplication : Application() {

    //Database used to access our SQL database (through Room API)
    private val database by lazy {
        ScooterDatabase.getDatabase(this)
    }

    //Scooter Repository - used for insertion, updating and deletion of Scooter
    val scooterRepository by lazy {
        ScooterRepository(database.scooterDao())
    }

    //History Repository - used for insertion, updating and deletion of History
    val historyRepository by lazy {
        HistoryRepository(database.historyDao())
    }

    //User Repository - used for insertion, updating and deletion of UserBalance
    val userRepository by lazy {
        UserBalanceRepository(database.userDao())
    }
}