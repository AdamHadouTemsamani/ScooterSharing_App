package dk.itu.moapd.scootersharing.ahad.database

import android.content.Context
import androidx.annotation.NonNull
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.getInstance
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dk.itu.moapd.scootersharing.ahad.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executors

@Database(entities = [Scooter::class, History::class, UserBalance::class], version = 1)
abstract class ScooterDatabase : RoomDatabase() {
    abstract fun scooterDao() : ScooterDao
    abstract fun historyDao() : HistoryDao
    abstract fun userDao() : UserDao

    companion object {
        @Volatile
        private var INSTANCE: ScooterDatabase? = null

        fun getDatabase(context: Context): ScooterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScooterDatabase::class.java,
                    "scooter_database"
                )
                    .addCallback(Prepopulate(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }


    class Prepopulate(private val context: Context) : RoomDatabase.Callback() {

        val date = Calendar.getInstance().time.minutes.toLong()
        val CPH01 = Scooter(
            0,
            "CPH01",
            "Nørre Voldgade 80A, 1358 København",
            date, date,
            12.569664388, 55.6833306, 12.569664388, 55.6833306,
            false, "CPH01.jpg"
        )


        val CPH02 = Scooter(
            0,
            "CPH02",
            "Rued Langgaards Vej 7, 2300 København",
            date, date,
            12.587997648, 55.65583071, 12.587997648, 55.65583071,
            false, "CPH02.jpg"
        )

        val CPH03 = Scooter(
            0,
            "CPH03",
            "Torvegade 47, 1400 København",
            date, date,
            12.592067113647936, 55.67230996820298, 12.592067113647936, 55.67230996820298,
            false, "CPH03.jpg"
        )
        val scooterList = listOf<Scooter>(CPH01, CPH02, CPH03)

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                populateWithScooters(context)
            }
        }

        private suspend fun populateWithScooters(context: Context) {
            val dao = ScooterDatabase.getDatabase(context).scooterDao()
            for(scooter in scooterList) {
                dao.insert(scooter)
            }
        }
    }

}

