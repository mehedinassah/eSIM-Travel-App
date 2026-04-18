package com.esim.travelapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esim.travelapp.data.repository.AuthRepository
import com.esim.travelapp.domain.model.User
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginState = MutableLiveData<AuthState>()
    val loginState: LiveData<AuthState> = _loginState

    private val _currentUser = MutableLiveData<User?>(null)
    val currentUser: LiveData<User?> = _currentUser

    fun login(email: String, password: String) {
        _loginState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.onSuccess { user ->
                _currentUser.value = user
                _loginState.value = AuthState.Success(user)
            }.onFailure { error ->
                _loginState.value = AuthState.Error(error.message ?: "Login failed")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        _loginState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.register(name, email, password)
            result.onSuccess { user ->
                _currentUser.value = user
                _loginState.value = AuthState.Success(user)
            }.onFailure { error ->
                _loginState.value = AuthState.Error(error.message ?: "Registration failed")
            }
        }
    }

    fun resetPassword(email: String) {
        _loginState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.resetPassword(email)
            result.onSuccess { message ->
                _loginState.value = AuthState.PasswordReset(message)
            }.onFailure { error ->
                _loginState.value = AuthState.Error(error.message ?: "Reset failed")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginState.value = AuthState.LoggedOut
    }

    fun resetState() {
        _loginState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
    data class PasswordReset(val message: String) : AuthState()
    object LoggedOut : AuthState()
}

class ESIMPlanViewModel(private val planRepository: com.esim.travelapp.data.repository.ESIMPlanRepository) : ViewModel() {
    val plans = planRepository.getAllPlans()

    fun getPlansByCountry(country: String) = planRepository.getPlansByCountry(country)
}

class PurchaseViewModel(private val purchaseRepository: com.esim.travelapp.data.repository.PurchaseRepository) : ViewModel() {

    private val _purchaseState = MutableLiveData<PurchaseState>()
    val purchaseState: LiveData<PurchaseState> = _purchaseState

    fun createPurchase(userId: Int, planId: Int) {
        _purchaseState.value = PurchaseState.Loading
        viewModelScope.launch {
            val result = purchaseRepository.createPurchase(userId, planId)
            result.onSuccess { purchaseId ->
                _purchaseState.value = PurchaseState.Success(purchaseId)
            }.onFailure { error ->
                _purchaseState.value = PurchaseState.Error(error.message ?: "Purchase failed")
            }
        }
    }

    fun getUserPurchases(userId: Int) = purchaseRepository.getUserPurchases(userId)
}

sealed class PurchaseState {
    object Loading : PurchaseState()
    data class Success(val purchaseId: Int) : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}

class NotificationViewModel(private val notificationRepository: com.esim.travelapp.data.repository.NotificationRepository) : ViewModel() {

    fun getUserNotifications(userId: Int) = notificationRepository.getUserNotifications(userId)
}
