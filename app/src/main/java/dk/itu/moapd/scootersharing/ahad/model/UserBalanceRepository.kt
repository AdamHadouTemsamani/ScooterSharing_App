package dk.itu.moapd.scootersharing.ahad.model

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.Flow

class UserBalanceRepository(private val userDao: UserDao) {

    val users: Flow<List<UserBalance>> = userDao.getUsers()

    @WorkerThread
    suspend fun insert(user: UserBalance) {
        userDao.insert(user)
    }

    @WorkerThread
    suspend fun update(user: UserBalance) {
        userDao.update(user)
    }

    @WorkerThread
    suspend fun delete(user: UserBalance) {
        userDao.delete(user)
    }

}



