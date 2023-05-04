package dk.itu.moapd.scootersharing.ahad.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(userBalance: UserBalance)

    @Update
    suspend fun update(userBalance: UserBalance)

    @Delete
    suspend fun delete(userBalance: UserBalance)

    @Query("SELECT * FROM userbalance")
    fun getUsers(): Flow<List<UserBalance>>

    @Query("SELECT * FROM userbalance WHERE email=(:email)")
    fun getUser(email: String): UserBalance
}