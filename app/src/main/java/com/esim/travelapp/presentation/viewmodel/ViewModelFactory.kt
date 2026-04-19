package com.esim.travelapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.esim.travelapp.data.repository.AuthRepository
import com.esim.travelapp.data.repository.ESIMPlanRepository
import com.esim.travelapp.data.repository.NotificationRepository
import com.esim.travelapp.data.repository.PurchaseRepository
import com.esim.travelapp.data.repository.PaymentRepository
import com.esim.travelapp.data.repository.ESIMActivationRepository
import com.esim.travelapp.data.repository.DataUsageRepository
import com.esim.travelapp.data.repository.UserRepository

class ViewModelFactory(
    private val authRepository: AuthRepository? = null,
    private val planRepository: ESIMPlanRepository? = null,
    private val purchaseRepository: PurchaseRepository? = null,
    private val notificationRepository: NotificationRepository? = null,
    private val paymentRepository: PaymentRepository? = null,
    private val activationRepository: ESIMActivationRepository? = null,
    private val dataUsageRepository: DataUsageRepository? = null,
    private val userRepository: UserRepository? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(authRepository!!) as T
            modelClass.isAssignableFrom(ESIMPlanViewModel::class.java) ->
                ESIMPlanViewModel(planRepository!!) as T
            modelClass.isAssignableFrom(PurchaseViewModel::class.java) ->
                PurchaseViewModel(purchaseRepository!!) as T
            modelClass.isAssignableFrom(NotificationViewModel::class.java) ->
                NotificationViewModel(notificationRepository!!) as T
            modelClass.isAssignableFrom(PaymentViewModel::class.java) ->
                PaymentViewModel(paymentRepository!!) as T
            modelClass.isAssignableFrom(ESIMActivationViewModel::class.java) ->
                ESIMActivationViewModel(activationRepository!!, dataUsageRepository!!) as T
            modelClass.isAssignableFrom(UserProfileViewModel::class.java) ->
                UserProfileViewModel(userRepository!!) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
