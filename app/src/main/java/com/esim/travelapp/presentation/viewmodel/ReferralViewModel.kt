package com.esim.travelapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.esim.travelapp.data.repository.ReferralRepository
import kotlinx.coroutines.launch

class ReferralViewModel(private val referralRepository: ReferralRepository) : ViewModel() {

    private val _referralState = MutableLiveData<ReferralState>()
    val referralState: LiveData<ReferralState> = _referralState

    fun generateReferralCode(userId: Int, discount: Double = 10.0) {
        _referralState.value = ReferralState.Loading
        viewModelScope.launch {
            val result = referralRepository.generateReferralCode(userId, discount)
            result.onSuccess { code ->
                _referralState.value = ReferralState.CodeGenerated(code)
            }.onFailure { error ->
                _referralState.value = ReferralState.Error(error.message ?: "Failed to generate code")
            }
        }
    }

    fun claimReferral(code: String, userId: Int) {
        _referralState.value = ReferralState.Loading
        viewModelScope.launch {
            val result = referralRepository.claimReferral(code, userId)
            result.onSuccess { discount ->
                _referralState.value = ReferralState.ReferralClaimed(discount)
            }.onFailure { error ->
                _referralState.value = ReferralState.Error(error.message ?: "Failed to claim referral")
            }
        }
    }

    fun getUserReferrals(userId: Int) = referralRepository.getUserReferrals(userId).asLiveData()

    fun getClaimedReferralCount(userId: Int) = referralRepository.getClaimedReferralCount(userId).asLiveData()

    fun getTotalBenefits(userId: Int) = referralRepository.getTotalReferralBenefits(userId).asLiveData()
}

sealed class ReferralState {
    object Loading : ReferralState()
    data class CodeGenerated(val code: String) : ReferralState()
    data class ReferralClaimed(val discount: Double) : ReferralState()
    data class Error(val message: String) : ReferralState()
}
