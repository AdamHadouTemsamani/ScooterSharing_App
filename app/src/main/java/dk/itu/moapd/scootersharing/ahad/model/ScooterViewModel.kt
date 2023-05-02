package dk.itu.moapd.scootersharing.ahad.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class ScooterViewModel(private val repository: ScooterRepository) : ViewModel() {

    val scooters : LiveData<List<Scooter>> = repository.scooters.asLiveData()


    fun insert(scooter: Scooter) = viewModelScope.launch {
        repository.insert(scooter)
    }

    fun update(scooter: Scooter) = viewModelScope.launch {
        repository.update(scooter)
    }

    fun delete(scooter: Scooter) = viewModelScope.launch {
        repository.delete(scooter)
    }



}

class ScooterViewModelFactory(private val repository: ScooterRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScooterViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return ScooterViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}