package dk.itu.moapd.scootersharing.ahad.model

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {

    val previousRides : LiveData<List<History>> = repository.previousRides.asLiveData()

    fun insert(previousRide: History) = viewModelScope.launch {
        repository.insert(previousRide)
    }


    fun delete(previousRide: History) = viewModelScope.launch {
        repository.delete(previousRide)
    }

    class HistoryViewModelFactory(private val repository: HistoryRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java))
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(repository) as T
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}