package dk.itu.moapd.scootersharing.ahad.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {

    val previousRides : LiveData<List<History>> = repository.previousRides.asLiveData()

    fun insert(previousRide: History) = viewModelScope.launch {
        repository.insert(previousRide)
    }


    fun delete(previousRide: History) = viewModelScope.launch {
        repository.delete(previousRide)
    }
}