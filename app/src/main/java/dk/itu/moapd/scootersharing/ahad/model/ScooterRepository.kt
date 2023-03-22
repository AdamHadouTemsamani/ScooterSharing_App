package dk.itu.moapd.scootersharing.ahad.model

import android.content.Context
import androidx.room.Room
import dk.itu.moapd.scootersharing.ahad.database.ScooterDatabase
import java.util.*

private const val DATABASE_NAME = "scooter-database"

class ScooterRepository private constructor(context: Context) {

    private val database : ScooterDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            ScooterDatabase::class.java,
            DATABASE_NAME
        )
        .build()

    suspend fun getScooters() : List<Scooter> = database.scooterDao().getScooters()
    suspend fun getScooter(id: UUID) : Scooter = database.scooterDao().getScooter(id)

    companion object {
        private var INSTANCE : ScooterRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = ScooterRepository(context)
            }
        }

        fun get() : ScooterRepository {
            return INSTANCE ?:
            throw IllegalStateException("ScooterRepostory must be initialized")
        }
    }
}