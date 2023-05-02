package dk.itu.moapd.scootersharing.ahad.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dk.itu.moapd.scootersharing.ahad.model.*
import dk.itu.moapd.scootersharing.ahad.utils.Converters

@Database(entities = [Scooter::class, History::class, UserBalance::class], version = 1)
abstract class ScooterDatabase : RoomDatabase() {
    abstract fun scooterDao() : ScooterDao
    abstract fun historyDao() : HistoryDao
    abstract fun userDao() : UserDao

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