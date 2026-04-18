package com.esim.travelapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.esim.travelapp.data.local.dao.ESIMPlanDao
import com.esim.travelapp.data.local.dao.NotificationDao
import com.esim.travelapp.data.local.dao.PurchaseDao
import com.esim.travelapp.data.local.dao.UserDao
import com.esim.travelapp.data.local.entity.ESIMPlanEntity
import com.esim.travelapp.data.local.entity.NotificationEntity
import com.esim.travelapp.data.local.entity.PurchaseEntity
import com.esim.travelapp.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ESIMPlanEntity::class,
        PurchaseEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun esimPlanDao(): ESIMPlanDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esim_travel_app.db"
                ).build().also { database ->
                    instance = database
                    // Seed data on first database creation
                    seedDatabase(database)
                }
            }

        private fun seedDatabase(database: AppDatabase) {
            Thread {
                val existingCount = database.esimPlanDao().getAllPlansSync()?.size ?: 0
                if (existingCount == 0) {
                    val plans = listOf(
                        ESIMPlanEntity(id = 1, country = "USA", planName = "USA 5GB Plan", dataAmount = "5GB", price = 19.99, validityDays = 7, description = "5GB data valid for 7 days in USA"),
                        ESIMPlanEntity(id = 2, country = "USA", planName = "USA 10GB Plan", dataAmount = "10GB", price = 34.99, validityDays = 15, description = "10GB data valid for 15 days in USA"),
                        ESIMPlanEntity(id = 3, country = "UK", planName = "UK 3GB Plan", dataAmount = "3GB", price = 15.99, validityDays = 7, description = "3GB data valid for 7 days in UK"),
                        ESIMPlanEntity(id = 4, country = "UK", planName = "UK 8GB Plan", dataAmount = "8GB", price = 29.99, validityDays = 14, description = "8GB data valid for 14 days in UK"),
                        ESIMPlanEntity(id = 5, country = "Canada", planName = "Canada 4GB Plan", dataAmount = "4GB", price = 17.99, validityDays = 7, description = "4GB data valid for 7 days in Canada"),
                        ESIMPlanEntity(id = 6, country = "Canada", planName = "Canada 12GB Plan", dataAmount = "12GB", price = 32.99, validityDays = 21, description = "12GB data valid for 21 days in Canada"),
                        ESIMPlanEntity(id = 7, country = "France", planName = "France 2GB Plan", dataAmount = "2GB", price = 14.99, validityDays = 7, description = "2GB data valid for 7 days in France"),
                        ESIMPlanEntity(id = 8, country = "France", planName = "France 6GB Plan", dataAmount = "6GB", price = 26.99, validityDays = 14, description = "6GB data valid for 14 days in France"),
                        ESIMPlanEntity(id = 9, country = "Japan", planName = "Japan 7GB Plan", dataAmount = "7GB", price = 22.99, validityDays = 10, description = "7GB data valid for 10 days in Japan"),
                        ESIMPlanEntity(id = 10, country = "Japan", planName = "Japan 15GB Plan", dataAmount = "15GB", price = 39.99, validityDays = 21, description = "15GB data valid for 21 days in Japan")
                    )
                    
                    for (plan in plans) {
                        database.esimPlanDao().insertSync(plan)
                    }
                }
            }.start()
        }
    }
}
