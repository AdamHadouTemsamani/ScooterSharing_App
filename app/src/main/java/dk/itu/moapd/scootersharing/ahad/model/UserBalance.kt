package dk.itu.moapd.scootersharing.ahad.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "userbalance")
data class UserBalance(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "balance") var balance: Double?,
    @ColumnInfo(name = "isCard") var isCard: Boolean = false
    ) {
}