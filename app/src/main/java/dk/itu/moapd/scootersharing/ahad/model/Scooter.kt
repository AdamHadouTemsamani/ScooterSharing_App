package dk.itu.moapd.scootersharing.ahad.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * A data class that encapsulates the information about a scooter
 */
@Entity(tableName = "scooter")
data class Scooter(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "location") var location: String?,
    @ColumnInfo(name = "startTime") var startTime: Long,
    @ColumnInfo(name = "endTime") var endTime: Long,
    @ColumnInfo(name = "URL") var URL: String = "CPH01"
){

    /**
     * Returns a formatted string with the information of the Scooter object
     * @return Returns String
     */
    override fun toString(): String {
        return "Ride $id started using Scooter(name=$name, location=$location, timestamp=$startTime)"
    }
}

