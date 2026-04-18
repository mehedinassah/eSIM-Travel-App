package com.esim.travelapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.esim.travelapp.data.repository.AuthRepository
import com.esim.travelapp.data.repository.ESIMPlanRepository
import com.esim.travelapp.data.repository.NotificationRepository
import com.esim.travelapp.data.repository.PurchaseRepository

class ViewModelFactory(
    private val authRepository: AuthRepository? = null,
    private val planRepository: ESIMPlanRepository? = null,
    private val purchaseRepository: PurchaseRepository? = null,
    private val notificationRepository: NotificationRepository? = null
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
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
