package com.esim.travelapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.repository.LocationRepository
import com.esim.travelapp.data.repository.PurchaseRepository
import com.esim.travelapp.presentation.viewmodel.LocationViewModel
import com.esim.travelapp.presentation.viewmodel.PurchaseViewModel
import com.esim.travelapp.presentation.viewmodel.ViewModelFactory
import com.esim.travelapp.ui.adapter.ActivePlanAdapter
import com.esim.travelapp.ui.purchase.PlanDetailsActivity
import com.esim.travelapp.ui.support.SupportActivity
import com.esim.travelapp.utils.LocationManager
import com.esim.travelapp.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private lateinit var purchaseViewModel: PurchaseViewModel
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var locationManager: LocationManager
    private var currentUserId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard_professional, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = PreferenceManager.getUserId(requireContext())
        locationManager = LocationManager(requireContext())

        setupViewModel()
        setupUI(view)
        loadActivePlans(view)
        detectUserLocation(view)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val purchaseRepository = PurchaseRepository(database.purchaseDao())
        val locationRepository = LocationRepository(database.locationDao())
        val factory = ViewModelFactory(purchaseRepository = purchaseRepository, locationRepository = locationRepository)
        purchaseViewModel = ViewModelProvider(this, factory).get(PurchaseViewModel::class.java)
        locationViewModel = ViewModelProvider(this, factory).get(LocationViewModel::class.java)
    }

    private fun setupUI(view: View) {
        val greetingUserName: TextView = view.findViewById(R.id.greetingUserName)
        val notificationBellButton: Button = view.findViewById(R.id.notificationBellButton)
        val refreshLocationButton: Button = view.findViewById(R.id.refreshLocationButton)
        val buyPlanQuickButton: Button = view.findViewById(R.id.buyPlanQuickButton)
        val topUpQuickButton: Button = view.findViewById(R.id.topUpQuickButton)
        val coverageQuickButton: Button = view.findViewById(R.id.coverageQuickButton)
        val supportQuickButton: Button = view.findViewById(R.id.supportQuickButton)
        val viewAllPlansButton: Button = view.findViewById(R.id.viewAllPlansButton)

        // Set greeting
        greetingUserName.text = PreferenceManager.getUserName(requireContext())

        // Notification bell button
        notificationBellButton.setOnClickListener {
            Toast.makeText(requireContext(), "Opening notifications", Toast.LENGTH_SHORT).show()
        }

        // Refresh location button
        refreshLocationButton.setOnClickListener {
            detectUserLocation(view)
        }

        // Buy plan button
        buyPlanQuickButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, StorefrontEnhancedFragment())
                .addToBackStack(null)
                .commit()
        }

        // Top-up button
        topUpQuickButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, StorefrontEnhancedFragment())
                .addToBackStack(null)
                .commit()
        }

        // Coverage button
        coverageQuickButton.setOnClickListener {
            Toast.makeText(requireContext(), "Opening coverage map", Toast.LENGTH_SHORT).show()
        }

        // Support button
        supportQuickButton.setOnClickListener {
            startActivity(Intent(requireContext(), SupportActivity::class.java))
        }

        // View all plans button
        viewAllPlansButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, StorefrontEnhancedFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadActivePlans(view: View) {
        val activePlansRecyclerView: RecyclerView = view.findViewById(R.id.activePlansRecyclerView)
        val noActivePlansLayout: LinearLayout = view.findViewById(R.id.noActivePlansLayout)

        activePlansRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        
        val activePlanAdapter = ActivePlanAdapter { purchase ->
            // Handle details button click
            Toast.makeText(requireContext(), "Plan details for purchase #${purchase.id}", Toast.LENGTH_SHORT).show()
        }
        activePlansRecyclerView.adapter = activePlanAdapter

        purchaseViewModel.getUserPurchases(currentUserId).asLiveData().observe(viewLifecycleOwner) { purchases ->
            // Show both pending and completed purchases as active plans
            val activePurchases = purchases.filter { it.status == "completed" || it.status == "pending" }
            
            if (activePurchases.isEmpty()) {
                noActivePlansLayout.visibility = View.VISIBLE
                activePlansRecyclerView.visibility = View.GONE
            } else {
                noActivePlansLayout.visibility = View.GONE
                activePlansRecyclerView.visibility = View.VISIBLE
                activePlanAdapter.submitList(activePurchases)
            }
        }
    }

    private fun detectUserLocation(view: View) {
        val dashboardLocationText: TextView = view.findViewById(R.id.dashboardLocationText)
        val locationDescriptionText: TextView = view.findViewById(R.id.locationDescriptionText)

        dashboardLocationText.text = "Detecting location..."
        
        CoroutineScope(Dispatchers.Main).launch {
            if (locationManager.hasLocationPermission()) {
                val location = locationManager.getLastLocation()
                if (location != null) {
                    val country = locationManager.findClosestCountry(location.first, location.second)
                    if (country != null) {
                        locationViewModel.saveUserLocation(
                            currentUserId,
                            location.first,
                            location.second,
                            country,
                            "Current Location"
                        )
                        dashboardLocationText.text = "🌍 $country"
                        locationDescriptionText.text = "Plans available for your region"
                    } else {
                        dashboardLocationText.text = "Location not recognized"
                    }
                } else {
                    dashboardLocationText.text = "Enable location services"
                }
            } else {
                dashboardLocationText.text = "Location permission required"
            }
        }
    }
}
