package dk.itu.moapd.scootersharing.ahad.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * A data class that encapsulates the information about a scooter
 */
@Entity
data class Scooter (
    @PrimaryKey val id: UUID,
    val name: String,
    var location: String,
    var timestamp: String) {

    /**
     * Returns a formatted string with the information of the Scooter object
     * @return Returns String
     */
    override fun toString(): String {
        return "Ride $id started using Scooter(name=$name, location=$location, timestamp=$timestamp)"
    }
}

