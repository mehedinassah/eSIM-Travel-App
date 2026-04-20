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

class PaymentViewModel(private val paymentRepository: com.esim.travelapp.data.repository.PaymentRepository) : ViewModel() {

    private val _paymentState = MutableLiveData<PaymentState>()
    val paymentState: LiveData<PaymentState> = _paymentState

    fun processPayment(userId: Int, purchaseId: Int, amount: Double, paymentMethod: String, transactionRef: String) {
        _paymentState.value = PaymentState.Processing
        viewModelScope.launch {
            val result = paymentRepository.createPayment(userId, purchaseId, amount, paymentMethod, transactionRef)
            result.onSuccess { paymentId ->
                // For testing: always succeed. In production, integrate real Stripe API
                val updateResult = paymentRepository.updatePaymentStatus(paymentId, "completed")
                updateResult.onSuccess {
                    _paymentState.value = PaymentState.Success(paymentId)
                }.onFailure { error ->
                    _paymentState.value = PaymentState.Error(error.message ?: "Failed to update payment status")
                }
            }.onFailure { error ->
                _paymentState.value = PaymentState.Error(error.message ?: "Payment processing failed")
            }
        }
    }
}

sealed class PaymentState {
    object Processing : PaymentState()
    data class Success(val paymentId: Int) : PaymentState()
    data class Failed(val message: String) : PaymentState()
    data class Error(val message: String) : PaymentState()
}

class ESIMActivationViewModel(private val activationRepository: com.esim.travelapp.data.repository.ESIMActivationRepository,
                              private val dataUsageRepository: com.esim.travelapp.data.repository.DataUsageRepository) : ViewModel() {

    private val _activationState = MutableLiveData<ActivationState>()
    val activationState: LiveData<ActivationState> = _activationState

    fun createActivation(purchaseId: Int, dataAmount: String) {
        _activationState.value = ActivationState.Creating
        viewModelScope.launch {
            val iccid = "8901${System.currentTimeMillis() % 10000000000}"
            val qrCode = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=$iccid"
            
            val result = activationRepository.createActivation(purchaseId, iccid, qrCode)
            result.onSuccess { activationId ->
                // Extract GB value from dataAmount (e.g., "5GB" -> 5.0)
                val gbs = dataAmount.replace("GB", "").toDoubleOrNull() ?: 1.0
                dataUsageRepository.createUsage(activationId, gbs).onSuccess {
                    _activationState.value = ActivationState.Created(activationId, iccid, qrCode)
                }.onFailure { error ->
                    _activationState.value = ActivationState.Error(error.message ?: "Failed to create usage data")
                }
            }.onFailure { error ->
                _activationState.value = ActivationState.Error(error.message ?: "Failed to create activation")
            }
        }
    }

    fun activateESIM(activationId: Int) {
        _activationState.value = ActivationState.Activating
        viewModelScope.launch {
            val result = activationRepository.activateESIM(activationId)
            result.onSuccess {
                _activationState.value = ActivationState.Activated
            }.onFailure { error ->
                _activationState.value = ActivationState.Error(error.message ?: "Activation failed")
            }
        }
    }
}

sealed class ActivationState {
    object Creating : ActivationState()
    object Activating : ActivationState()
    data class Created(val activationId: Int, val iccid: String, val qrCode: String) : ActivationState()
    object Activated : ActivationState()
    data class Error(val message: String) : ActivationState()
}

class UserProfileViewModel(private val userRepository: com.esim.travelapp.data.repository.UserRepository) : ViewModel() {

    private val _profileState = MutableLiveData<ProfileState>()
    val profileState: LiveData<ProfileState> = _profileState

    fun updateProfile(userId: Int, name: String, email: String) {
        _profileState.value = ProfileState.Updating
        viewModelScope.launch {
            val result = userRepository.updateUserProfile(userId, name, email)
            result.onSuccess {
                _profileState.value = ProfileState.Updated
            }.onFailure { error ->
                _profileState.value = ProfileState.Error(error.message ?: "Profile update failed")
            }
        }
    }

    suspend fun getUserProfile(userId: Int): User? {
        return userRepository.getUserById(userId)?.let {
            User(
                id = it.id,
                name = it.name,
                email = it.email
            )
        }
    }
}

sealed class ProfileState {
    object Updating : ProfileState()
    object Updated : ProfileState()
    data class Error(val message: String) : ProfileState()
}
