package dk.itu.moapd.scootersharing.ahad.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

// A data class that encapsulates information about a previously active ride.
@Entity(tableName = "history")
data class History(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "location") var location: String?,
    @ColumnInfo(name = "time") var time: Long,
    @ColumnInfo(name = "startLong") var startLong: Double,
    @ColumnInfo(name = "startLat") var startLat: Double,
    @ColumnInfo(name = "endLong") var endLong: Double,
    @ColumnInfo(name = "endLat") var endLat: Double,
    @ColumnInfo(name = "price") var price: Int,
    @ColumnInfo(name = "URL") var URL: String = "CPH01"
){
}