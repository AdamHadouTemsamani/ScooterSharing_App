package dk.itu.moapd.scootersharing.ahad.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class History (
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "scooter") val scooter: Scooter?) {
}