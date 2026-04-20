package com.esim.travelapp.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.esim.travelapp.R
import com.esim.travelapp.ui.BaseActivity
import com.esim.travelapp.ui.fragments.DashboardFragment
import com.esim.travelapp.ui.fragments.NotificationsFragment
import com.esim.travelapp.ui.fragments.ProfileFragment
import com.esim.travelapp.ui.fragments.StorefrontEnhancedFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : BaseActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Get FCM Token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "getInstanceId failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "FCM Token: $token")
        }

        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> loadFragment(DashboardFragment())
                R.id.nav_storefront -> loadFragment(StorefrontEnhancedFragment())
                R.id.nav_notifications -> loadFragment(NotificationsFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
                else -> false
            }
            true
        }

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
            bottomNavigation.selectedItemId = R.id.nav_dashboard
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
