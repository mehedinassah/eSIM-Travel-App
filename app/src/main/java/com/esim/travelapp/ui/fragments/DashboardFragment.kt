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
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
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
import com.esim.travelapp.ui.adapter.ActivePlanDisplayModel
import com.esim.travelapp.ui.support.SupportActivity
import com.esim.travelapp.utils.LocationManager
import com.esim.travelapp.utils.PreferenceManager
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment() {

    private lateinit var purchaseViewModel: PurchaseViewModel
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var locationManager: LocationManager
    private var currentUserId: Int = 0
    private lateinit var database: AppDatabase
    private var fragmentView: View? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Permission granted, detect location
            fragmentView?.let { view ->
                if (isAdded) {
                    detectUserLocation(view)
                }
            }
        } else {
            // Permission denied, show default location
            fragmentView?.let { view ->
                if (isAdded) {
                    detectUserLocation(view)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard_professional, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentView = view
        currentUserId = PreferenceManager.getUserId(requireContext())
        locationManager = LocationManager(requireContext())
        database = AppDatabase.getInstance(requireContext())

        setupViewModel()
        setupUI(view)
        loadActivePlans(view)
        
        // Request location permission on first login using Android's native system
        if (!PreferenceManager.hasLocationPermissionBeenAsked(requireContext())) {
            requestLocationPermission(view)
        } else {
            detectUserLocation(view)
        }
    }

    private fun setupViewModel() {
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
        val activePlanCountText: TextView = view.findViewById(R.id.activePlanCountText)
        val noActivePlansLayout: LinearLayout = view.findViewById(R.id.noActivePlansLayout)
        val activePlanSummaryCard: CardView = view.findViewById(R.id.activePlanSummaryCard)
        val activePlanSummaryName: TextView = view.findViewById(R.id.activePlanSummaryName)
        val activePlanSummaryData: TextView = view.findViewById(R.id.activePlanSummaryData)
        val activePlanSummaryRemaining: TextView = view.findViewById(R.id.activePlanSummaryRemaining)
        val activePlanSummaryProgress: ProgressBar = view.findViewById(R.id.activePlanSummaryProgress)
        val activePlanSummaryValidity: TextView = view.findViewById(R.id.activePlanSummaryValidity)

        // Start in empty-state mode until data is loaded.
        noActivePlansLayout.visibility = View.VISIBLE
        activePlansRecyclerView.visibility = View.GONE
        activePlanSummaryCard.visibility = View.GONE
        activePlanCountText.text = "0 active"

        activePlansRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        activePlansRecyclerView.setHasFixedSize(true)
        
        val activePlanAdapter = ActivePlanAdapter(
            onDetailsClick = { purchase ->
                Toast.makeText(requireContext(), "Plan details for purchase #${purchase.id}", Toast.LENGTH_SHORT).show()
            },
            onRenewClick = { purchase ->
                Toast.makeText(requireContext(), "Renewing plan #${purchase.id}", Toast.LENGTH_SHORT).show()
                // Navigate to storefront for renewal
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, StorefrontEnhancedFragment())
                    .addToBackStack(null)
                    .commit()
            }
        )
        activePlansRecyclerView.adapter = activePlanAdapter

        // Load purchases and enrich with plan details and data usage
        purchaseViewModel.getUserPurchases(currentUserId).asLiveData().observe(viewLifecycleOwner) { purchases ->
            // Show only completed purchases that have an activated eSIM
            val completedPurchases = purchases.filter { it.status == "completed" }
            
            if (completedPurchases.isEmpty()) {
                noActivePlansLayout.visibility = View.VISIBLE
                activePlansRecyclerView.visibility = View.GONE
                activePlanCountText.text = "0 active"
            } else {
                // Enrich purchases with plan details and usage data
                viewLifecycleOwner.lifecycleScope.launch {
                    val displayModels = withContext(Dispatchers.IO) {
                        completedPurchases.mapNotNull { purchase ->
                            try {
                                // Get plan details
                                val plan = database.esimPlanDao().getPlanById(purchase.planId)
                                    ?: return@mapNotNull null

                                // Get activation for this purchase
                                val activation = database.esimActivationDao().getActivationByPurchaseId(purchase.id)

                                if (activation?.activationStatus != "activated") {
                                    return@mapNotNull null
                                }

                                // Get data usage if activation exists
                                val usage = activation.let {
                                    database.dataUsageDao().getUsageByActivationId(it.id)
                                }

                                ActivePlanDisplayModel(purchase, plan, usage)
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }

                    if (displayModels.isEmpty()) {
                        noActivePlansLayout.visibility = View.VISIBLE
                        activePlansRecyclerView.visibility = View.GONE
                        activePlanSummaryCard.visibility = View.GONE
                        activePlanCountText.text = "0 active"
                    } else {
                        noActivePlansLayout.visibility = View.GONE
                        activePlansRecyclerView.visibility = View.VISIBLE
                        activePlanSummaryCard.visibility = View.VISIBLE
                        activePlanCountText.text = "${displayModels.size} active"

                        val firstPlan = displayModels.first()
                        val usage = firstPlan.dataUsage
                        val remainingPercent = if (usage != null && usage.dataTotal > 0) {
                            ((usage.dataRemaining / usage.dataTotal) * 100).toInt().coerceIn(0, 100)
                        } else {
                            100
                        }

                        activePlanSummaryName.text = firstPlan.plan.planName
                        activePlanSummaryData.text = "Data: ${firstPlan.plan.dataAmount}"
                        activePlanSummaryRemaining.text = if (usage != null) {
                            "${remainingPercent}% remaining (${String.format("%.1f", usage.dataRemaining)}/${String.format("%.1f", usage.dataTotal)} GB)"
                        } else {
                            "100% remaining (${firstPlan.plan.dataAmount})"
                        }
                        activePlanSummaryProgress.progress = remainingPercent
                        activePlanSummaryValidity.text = "Validity: ${firstPlan.plan.validityDays} days"

                        activePlanAdapter.submitList(displayModels) {
                            activePlansRecyclerView.scrollToPosition(0)
                        }
                    }
                }
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
                dashboardLocationText.text = "🌍 USA"
                locationDescriptionText.text = "Default location (enable location to personalize)"
                // Save default location for user
                locationViewModel.saveUserLocation(
                    currentUserId,
                    37.0902,
                    -95.7129,
                    "USA",
                    "Default Location"
                )
            }
        }
    }

    private fun requestLocationPermission(view: View) {
        try {
            // Check if permission is already granted
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted
                PreferenceManager.setLocationPermissionAsked(requireContext())
                detectUserLocation(view)
            } else {
                // Request permission using Android's native system dialog
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PreferenceManager.setLocationPermissionAsked(requireContext())
            if (isAdded && fragmentView != null) {
                detectUserLocation(fragmentView!!)
            }
        }
    }
}
