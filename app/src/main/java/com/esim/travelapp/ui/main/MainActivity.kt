package com.esim.travelapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.esim.travelapp.R
import com.esim.travelapp.ui.BaseActivity
import com.esim.travelapp.ui.fragments.DashboardFragment
import com.esim.travelapp.ui.fragments.NotificationsFragment
import com.esim.travelapp.ui.fragments.ProfileFragment
import com.esim.travelapp.ui.fragments.StorefrontEnhancedFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.esim.travelapp.service.DataUsageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : BaseActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase (lightweight, safe on main thread)
        FirebaseApp.initializeApp(this)

        // Start DataUsageService off main thread to avoid ANR
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                // Small delay to let the UI render first
                kotlinx.coroutines.delay(500)
            }
            startService(Intent(this@MainActivity, DataUsageService::class.java))
        }

        // Get FCM Token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "getInstanceId failed", task.exception)
                return@addOnCompleteListener
            }
            Log.d("FCM", "FCM Token: ${task.result}")
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
