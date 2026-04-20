package com.esim.travelapp.data.repository

import com.esim.travelapp.data.local.dao.WishlistDao
import com.esim.travelapp.data.local.entity.WishlistEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WishlistRepository(private val wishlistDao: WishlistDao) {

    suspend fun addToWishlist(userId: Int, planId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val wishlist = WishlistEntity(userId = userId, planId = planId)
                wishlistDao.addToWishlist(wishlist)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun removeFromWishlist(userId: Int, planId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                wishlistDao.removeWishlistItem(userId, planId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun getUserWishlist(userId: Int) = wishlistDao.getUserWishlist(userId)

    suspend fun isInWishlist(userId: Int, planId: Int): Boolean =
        withContext(Dispatchers.IO) {
            try {
                wishlistDao.isInWishlist(userId, planId) > 0
            } catch (e: Exception) {
                false
            }
        }
}
