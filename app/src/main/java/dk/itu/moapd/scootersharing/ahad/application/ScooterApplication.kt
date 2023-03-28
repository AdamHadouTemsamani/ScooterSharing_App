package dk.itu.moapd.scootersharing.ahad.application

import android.app.Application
import dk.itu.moapd.scootersharing.ahad.database.ScooterDatabase
import dk.itu.moapd.scootersharing.ahad.model.ScooterRepository

inline fun <reified T> T.TAG(): String = T::class.java.simpleName
class ScooterApplication : Application() {

    private val database by lazy {
        ScooterDatabase.getDatabase(this)
    }

    val repository by lazy {
        ScooterRepository(database.scooterDao())
    }
}