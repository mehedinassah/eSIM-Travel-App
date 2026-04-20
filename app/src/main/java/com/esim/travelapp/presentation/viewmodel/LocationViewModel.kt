package com.esim.travelapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.esim.travelapp.data.repository.LocationRepository
import com.esim.travelapp.data.local.entity.UserLocationEntity
import kotlinx.coroutines.launch

class LocationViewModel(private val locationRepository: LocationRepository) : ViewModel() {

    private val _locationState = MutableLiveData<LocationState>()
    val locationState: LiveData<LocationState> = _locationState

    fun saveUserLocation(
        userId: Int,
        latitude: Double,
        longitude: Double,
        country: String,
        city: String
    ) {
        _locationState.value = LocationState.Loading
        viewModelScope.launch {
            val result = locationRepository.saveUserLocation(userId, latitude, longitude, country, city)
            result.onSuccess {
                _locationState.value = LocationState.Success("Location saved")
            }.onFailure { error ->
                _locationState.value = LocationState.Error(error.message ?: "Failed to save location")
            }
        }
    }

    fun getUserLocation(userId: Int) = locationRepository.getUserLocation(userId).asLiveData()

    fun getUserCountry(userId: Int): LiveData<String?> {
        val country = MutableLiveData<String?>()
        viewModelScope.launch {
            country.value = locationRepository.getUserCountry(userId)
        }
        return country
    }
}

sealed class LocationState {
    object Loading : LocationState()
    data class Success(val message: String) : LocationState()
    data class Error(val message: String) : LocationState()
}
