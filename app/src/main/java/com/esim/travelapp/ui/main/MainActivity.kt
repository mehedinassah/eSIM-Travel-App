package com.esim.travelapp.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.esim.travelapp.R
import com.esim.travelapp.ui.BaseActivity
import com.esim.travelapp.ui.auth.LoginActivity
import com.esim.travelapp.ui.fragments.DashboardFragment
import com.esim.travelapp.ui.fragments.NotificationsFragment
import com.esim.travelapp.ui.fragments.ProfileFragment
import com.esim.travelapp.ui.fragments.StorefrontFragment

class MainActivity : BaseActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> loadFragment(DashboardFragment())
                R.id.nav_storefront -> loadFragment(StorefrontFragment())
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
