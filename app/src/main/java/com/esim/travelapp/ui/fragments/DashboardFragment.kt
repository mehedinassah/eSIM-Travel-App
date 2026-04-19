package com.esim.travelapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.local.entity.PurchaseEntity
import com.esim.travelapp.data.repository.PurchaseRepository
import com.esim.travelapp.presentation.viewmodel.PurchaseViewModel
import com.esim.travelapp.presentation.viewmodel.ViewModelFactory
import com.esim.travelapp.utils.PreferenceManager

class DashboardFragment : Fragment() {

    private lateinit var purchaseViewModel: PurchaseViewModel
    private var currentUserId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = PreferenceManager.getUserId(requireContext())

        setupViewModel()
        setupUI(view)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val purchaseRepository = PurchaseRepository(database.purchaseDao())
        val factory = ViewModelFactory(purchaseRepository = purchaseRepository)
        purchaseViewModel = ViewModelProvider(this, factory).get(PurchaseViewModel::class.java)
    }

    private fun setupUI(view: View) {
        val dataRemainingText: TextView = view.findViewById(R.id.dataRemainingText)
        val daysLeftText: TextView = view.findViewById(R.id.daysLeftText)
        val buyPlanButton: Button = view.findViewById(R.id.buyPlanButton)
        val topUpButton: Button = view.findViewById(R.id.topUpButton)

        // Observe purchases
        purchaseViewModel.getUserPurchases(currentUserId).asLiveData().observe(viewLifecycleOwner) { purchases ->
            if (purchases.isNotEmpty()) {
                val latestPurchase = purchases.last()
                val statusText = when (latestPurchase.status) {
                    "pending" -> "Pending"
                    "completed" -> "Active"
                    else -> "Inactive"
                }
                dataRemainingText.text = "Status: $statusText"
                
                // Calculate days remaining from purchase
                val createdAt = latestPurchase.createdAt
                val now = System.currentTimeMillis()
                val daysElapsed = (now - createdAt) / (1000 * 60 * 60 * 24)
                val daysRemaining = 30 - daysElapsed // Assume 30-day plans
                daysLeftText.text = if (daysRemaining > 0) "$daysRemaining days left" else "Expired"
            } else {
                dataRemainingText.text = "No active plan"
                daysLeftText.text = "Purchase a plan to get started"
            }
        }

        buyPlanButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, StorefrontFragment())
                .addToBackStack(null)
                .commit()
        }

        topUpButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, StorefrontFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
