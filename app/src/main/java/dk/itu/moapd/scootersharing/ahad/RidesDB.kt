package dk.itu.moapd.scootersharing.ahad

import android.content.Context
import java.util.*
import kotlin.collections.ArrayList

class RidesDB private constructor(context: Context) {

    private val rides = ArrayList<Scooter>()
    private var currentScooter = Scooter("name", "location", Calendar.getInstance().time.toString())

    companion object : RidesDBHolder<RidesDB, Context>(::RidesDB)

    init {
        rides.add(
            Scooter("CPH001", "ITU", Calendar.getInstance().time.toString())
        )
        rides.add(
            Scooter("CPH002", "Fields", Calendar.getInstance().time.toString())
        )
        rides.add(
            Scooter("CPH003", "Lufthavn", Calendar.getInstance().time.toString())
        )
        // TODO : You can add more ‘ Scooter ‘ objects if you want to .
    }

    fun getRidesList(): List<Scooter> {
        return rides
    }

    fun addScooter(name: String, location: String, date: String) {
        currentScooter = Scooter(name, location, date)
        if (!rides.contains(currentScooter)) {
            rides.add(currentScooter)
            return
        }
        print("Scooter already exists")
    }

    fun deleteScooter(scooter: Scooter) {
        rides.remove(scooter)
    }

    fun updateCurrentScooter(location: String, timestampt: Long) {
        currentScooter.location = location
    }

    fun getCurrentScooter(): Scooter {
        return currentScooter
    }

    fun getCurrentScooterInfo(): String {
        return currentScooter.toString()
    }

}

open class RidesDBHolder<out T : Any, in A>(creator: (A) -> T) {

    private var creator: ((A) -> T)? = creator

    @Volatile
    private var instance: T? = null

    fun get(arg: A): T {
        val checkInstance = instance
        if (checkInstance != null)
            return checkInstance

        return synchronized(this) {
            val checkInstanceAgain = instance
            if (checkInstanceAgain != null)
                checkInstanceAgain
            else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}

