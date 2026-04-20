package com.esim.travelapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.esim.travelapp.data.local.entity.WishlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Insert
    suspend fun addToWishlist(wishlist: WishlistEntity)

    @Delete
    suspend fun removeFromWishlist(wishlist: WishlistEntity)

    @Query("SELECT * FROM wishlist WHERE userId = :userId ORDER BY addedAt DESC")
    fun getUserWishlist(userId: Int): Flow<List<WishlistEntity>>

    @Query("SELECT * FROM wishlist WHERE userId = :userId AND planId = :planId LIMIT 1")
    suspend fun getWishlistItem(userId: Int, planId: Int): WishlistEntity?

    @Query("DELETE FROM wishlist WHERE userId = :userId AND planId = :planId")
    suspend fun removeWishlistItem(userId: Int, planId: Int)

    @Query("SELECT COUNT(*) FROM wishlist WHERE userId = :userId AND planId = :planId")
    suspend fun isInWishlist(userId: Int, planId: Int): Int
}
