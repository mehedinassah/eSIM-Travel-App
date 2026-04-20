package com.esim.travelapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.esim.travelapp.data.repository.WishlistRepository
import kotlinx.coroutines.launch

class WishlistViewModel(private val wishlistRepository: WishlistRepository) : ViewModel() {

    private val _addState = MutableLiveData<WishlistState>()
    val addState: LiveData<WishlistState> = _addState

    fun addToWishlist(userId: Int, planId: Int) {
        _addState.value = WishlistState.Loading
        viewModelScope.launch {
            val result = wishlistRepository.addToWishlist(userId, planId)
            result.onSuccess {
                _addState.value = WishlistState.Success("Added to wishlist")
            }.onFailure { error ->
                _addState.value = WishlistState.Error(error.message ?: "Failed to add")
            }
        }
    }

    fun removeFromWishlist(userId: Int, planId: Int) {
        viewModelScope.launch {
            wishlistRepository.removeFromWishlist(userId, planId)
        }
    }

    fun getUserWishlist(userId: Int) = wishlistRepository.getUserWishlist(userId).asLiveData()

    fun isInWishlist(userId: Int, planId: Int): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch {
            result.value = wishlistRepository.isInWishlist(userId, planId)
        }
        return result
    }
}

sealed class WishlistState {
    object Loading : WishlistState()
    data class Success(val message: String) : WishlistState()
    data class Error(val message: String) : WishlistState()
}
