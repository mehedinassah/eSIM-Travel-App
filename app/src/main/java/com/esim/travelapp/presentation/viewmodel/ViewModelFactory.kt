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
import com.esim.travelapp.data.repository.WishlistRepository
import com.esim.travelapp.data.repository.LocationRepository
import com.esim.travelapp.data.repository.AnalyticsRepository
import com.esim.travelapp.data.repository.SupportRepository
import com.esim.travelapp.data.repository.ReferralRepository
import com.esim.travelapp.data.repository.AutoRenewalRepository

class ViewModelFactory(
    private val authRepository: AuthRepository? = null,
    private val planRepository: ESIMPlanRepository? = null,
    private val purchaseRepository: PurchaseRepository? = null,
    private val notificationRepository: NotificationRepository? = null,
    private val paymentRepository: PaymentRepository? = null,
    private val activationRepository: ESIMActivationRepository? = null,
    private val dataUsageRepository: DataUsageRepository? = null,
    private val userRepository: UserRepository? = null,
    private val wishlistRepository: WishlistRepository? = null,
    private val locationRepository: LocationRepository? = null,
    private val analyticsRepository: AnalyticsRepository? = null,
    private val supportRepository: SupportRepository? = null,
    private val referralRepository: ReferralRepository? = null,
    private val autoRenewalRepository: AutoRenewalRepository? = null
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
            modelClass.isAssignableFrom(WishlistViewModel::class.java) ->
                WishlistViewModel(wishlistRepository!!) as T
            modelClass.isAssignableFrom(LocationViewModel::class.java) ->
                LocationViewModel(locationRepository!!) as T
            modelClass.isAssignableFrom(SupportViewModel::class.java) ->
                SupportViewModel(supportRepository!!) as T
            modelClass.isAssignableFrom(ReferralViewModel::class.java) ->
                ReferralViewModel(referralRepository!!) as T
            modelClass.isAssignableFrom(AutoRenewalViewModel::class.java) ->
                AutoRenewalViewModel(autoRenewalRepository!!) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
