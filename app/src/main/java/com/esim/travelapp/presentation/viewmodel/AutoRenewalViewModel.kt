package com.esim.travelapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.esim.travelapp.data.repository.AutoRenewalRepository
import kotlinx.coroutines.launch

class AutoRenewalViewModel(private val autoRenewalRepository: AutoRenewalRepository) : ViewModel() {

    private val _renewalState = MutableLiveData<RenewalState>()
    val renewalState: LiveData<RenewalState> = _renewalState

    fun enableAutoRenewal(userId: Int, planId: Int, renewalThreshold: Double = 10.0) {
        _renewalState.value = RenewalState.Loading
        viewModelScope.launch {
            val result = autoRenewalRepository.enableAutoRenewal(userId, planId, renewalThreshold)
            result.onSuccess {
                _renewalState.value = RenewalState.Success("Auto-renewal enabled")
            }.onFailure { error ->
                _renewalState.value = RenewalState.Error(error.message ?: "Failed to enable auto-renewal")
            }
        }
    }

    fun disableAutoRenewal(userId: Int, planId: Int) {
        viewModelScope.launch {
            autoRenewalRepository.disableAutoRenewal(userId, planId)
        }
    }

    fun getUserAutoRenewals(userId: Int) = autoRenewalRepository.getUserAutoRenewals(userId).asLiveData()

    fun updateRenewalThreshold(userId: Int, planId: Int, newThreshold: Double) {
        _renewalState.value = RenewalState.Loading
        viewModelScope.launch {
            val result = autoRenewalRepository.updateRenewalThreshold(userId, planId, newThreshold)
            result.onSuccess {
                _renewalState.value = RenewalState.Success("Renewal threshold updated")
            }.onFailure { error ->
                _renewalState.value = RenewalState.Error(error.message ?: "Failed to update")
            }
        }
    }
}

sealed class RenewalState {
    object Loading : RenewalState()
    data class Success(val message: String) : RenewalState()
    data class Error(val message: String) : RenewalState()
}
