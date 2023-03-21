package dk.itu.moapd.scootersharing.ahad.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dk.itu.moapd.scootersharing.ahad.model.Scooter
import dk.itu.moapd.scootersharing.ahad.model.ScooterDao

@Database(entities = [Scooter::class], version = 1)
abstract class ScooterDatabase : RoomDatabase() {
    abstract fun scooterDao() : ScooterDao
}