package dk.itu.moapd.scootersharing.ahad.model

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class UserBalanceViewModel(private val repository: UserBalanceRepository) : ViewModel() {

    val users: LiveData<List<UserBalance>> = repository.users.asLiveData()

    fun insert(user: UserBalance) = viewModelScope.launch {
        repository.insert(user)
    }

    fun update(user: UserBalance) = viewModelScope.launch {
        repository.update(user)
    }

    fun delete(user: UserBalance) = viewModelScope.launch {
        repository.delete(user)
    }
}

class UserBalanceViewModelFactory(private val repository: UserBalanceRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserBalanceViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return UserBalanceViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}