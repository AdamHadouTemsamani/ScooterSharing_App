package dk.itu.moapd.scootersharing.ahad.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dk.itu.moapd.scootersharing.ahad.model.Scooter
import dk.itu.moapd.scootersharing.ahad.model.ScooterDao
import dk.itu.moapd.scootersharing.ahad.model.ScooterRepository

@Database(entities = [Scooter::class], version = 1)
abstract class ScooterDatabase : RoomDatabase() {
    abstract fun scooterDao() : ScooterDao

    companion object {
        @Volatile
        private var INSTANCE : ScooterDatabase? = null

        fun getDatabase(context: Context) : ScooterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        ScooterDatabase::class.java,
                        "scooter_database"
                        ).build()

                        INSTANCE = instance

                        instance
            }
        }
    }
}