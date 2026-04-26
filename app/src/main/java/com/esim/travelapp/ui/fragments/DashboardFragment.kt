package com.esim.travelapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.local.entity.ESIMActivationEntity
import com.esim.travelapp.data.local.entity.ESIMPlanEntity
import com.esim.travelapp.data.local.entity.PurchaseEntity
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment() {

    private lateinit var purchaseViewModel: PurchaseViewModel
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var locationManager: LocationManager
    private var currentUserId: Int = 0
    private lateinit var database: AppDatabase
    private var fragmentView: View? = null
    private var usageObserverJob: Job? = null

    private var activePlanSummaryCard: CardView? = null
    private var activePlanSummaryName: TextView? = null
    private var activePlanSummaryData: TextView? = null
    private var activePlanSummaryRemaining: TextView? = null
    private var activePlanSummaryProgress: ProgressBar? = null
    private var activePlanSummaryValidity: TextView? = null
    private var activePlanCountText: TextView? = null
    private var noActivePlansLayout: LinearLayout? = null
    private var activePlansRecyclerView: RecyclerView? = null
    private var activePlanAdapter: ActivePlanAdapter? = null

    // Holds all active plan info for aggregate calculations
    private data class ActivePlanInfo(
        val purchase: PurchaseEntity,
        val plan: ESIMPlanEntity,
        val activation: ESIMActivationEntity?
    )

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        fragmentView?.let { if (isAdded) detectUserLocation(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

        if (!PreferenceManager.hasLocationPermissionBeenAsked(requireContext())) {
            requestLocationPermission(view)
        } else {
            detectUserLocation(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        usageObserverJob?.cancel()
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
        val supportQuickButton: Button = view.findViewById(R.id.supportQuickButton)
        val viewAllPlansButton: Button = view.findViewById(R.id.viewAllPlansButton)

        greetingUserName.text = PreferenceManager.getUserName(requireContext())
        notificationBellButton.setOnClickListener {
            Toast.makeText(requireContext(), "Opening notifications", Toast.LENGTH_SHORT).show()
        }
        refreshLocationButton.setOnClickListener { detectUserLocation(view) }
        buyPlanQuickButton.setOnClickListener { navigateToStorefront() }
        topUpQuickButton.setOnClickListener { navigateToStorefront() }
        supportQuickButton.setOnClickListener {
            startActivity(Intent(requireContext(), SupportActivity::class.java))
        }
        viewAllPlansButton.setOnClickListener { navigateToStorefront() }
    }

    private fun navigateToStorefront() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, StorefrontEnhancedFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun loadActivePlans(view: View) {
        activePlansRecyclerView    = view.findViewById(R.id.activePlansRecyclerView)
        activePlanCountText        = view.findViewById(R.id.activePlanCountText)
        noActivePlansLayout        = view.findViewById(R.id.noActivePlansLayout)
        activePlanSummaryCard      = view.findViewById(R.id.activePlanSummaryCard)
        activePlanSummaryName      = view.findViewById(R.id.activePlanSummaryName)
        activePlanSummaryData      = view.findViewById(R.id.activePlanSummaryData)
        activePlanSummaryRemaining = view.findViewById(R.id.activePlanSummaryRemaining)
        activePlanSummaryProgress  = view.findViewById(R.id.activePlanSummaryProgress)
        activePlanSummaryValidity  = view.findViewById(R.id.activePlanSummaryValidity)

        showEmptyState()

        activePlansRecyclerView?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        activePlansRecyclerView?.setHasFixedSize(true)

        activePlanAdapter = ActivePlanAdapter(
            onDetailsClick = { purchase ->
                Toast.makeText(requireContext(), "Plan: Purchase #${purchase.id}", Toast.LENGTH_SHORT).show()
            },
            onRenewClick = { _ -> navigateToStorefront() }
        )
        activePlansRecyclerView?.adapter = activePlanAdapter

        purchaseViewModel.getUserPurchases(currentUserId).asLiveData()
            .observe(viewLifecycleOwner) { purchases ->
                val completedPurchases = purchases.filter { it.status == "completed" }
                if (completedPurchases.isEmpty()) {
                    showEmptyState()
                    return@observe
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    val activePlanInfoList = withContext(Dispatchers.IO) {
                        completedPurchases.mapNotNull { purchase ->
                            try {
                                val plan = database.esimPlanDao().getPlanById(purchase.planId)
                                    ?: return@mapNotNull null
                                val activation = database.esimActivationDao()
                                    .getActivationByPurchaseId(purchase.id)
                                ActivePlanInfo(purchase, plan, activation)
                            } catch (e: Exception) { null }
                        }
                    }

                    if (activePlanInfoList.isEmpty()) {
                        showEmptyState()
                        return@launch
                    }

                    // Build display models for the horizontal list
                    val displayModels = withContext(Dispatchers.IO) {
                        activePlanInfoList.map { info ->
                            val usage = info.activation?.let {
                                database.dataUsageDao().getUsageByActivationId(it.id)
                            }
                            ActivePlanDisplayModel(info.purchase, info.plan, usage)
                        }
                    }

                    showActivePlans(displayModels)

                    // Start aggregate live observer
                    val activationIds = activePlanInfoList.mapNotNull { it.activation?.id }
                    startAggregateUsageObserver(activePlanInfoList, activationIds)
                }
            }
    }

    /**
     * Observes ALL active plans' data usage simultaneously.
     * Summary card shows:
     *  - Total data = sum of all plan dataTotal
     *  - Remaining = sum of all dataRemaining (live, decreases 1%/sec per plan)
     *  - Validity = max validityDays across all plans
     *  - Progress bar = combined remaining %
     */
    private fun startAggregateUsageObserver(
        planInfoList: List<ActivePlanInfo>,
        activationIds: List<Int>
    ) {
        usageObserverJob?.cancel()

        // Aggregate plan-level data (doesn't change)
        val totalPlanGB = planInfoList.sumOf { extractGB(it.plan.dataAmount) }
        val maxValidity = planInfoList.maxOf { it.plan.validityDays }
        val planCount = planInfoList.size

        // Update static fields immediately
        activePlanSummaryName?.text = if (planCount == 1)
            planInfoList.first().plan.planName
        else
            "$planCount Active Plans"
        activePlanSummaryData?.text = "Total Data: ${formatGB(totalPlanGB)}"
        activePlanSummaryValidity?.text = "Max Validity: $maxValidity days"
        activePlanSummaryCard?.visibility = View.VISIBLE

        if (activationIds.isEmpty()) {
            // No activations yet — show 100%
            activePlanSummaryRemaining?.text = "100% remaining (${formatGB(totalPlanGB)} / ${formatGB(totalPlanGB)})"
            activePlanSummaryProgress?.progress = 100
            return
        }

        usageObserverJob = viewLifecycleOwner.lifecycleScope.launch {
            database.dataUsageDao()
                .observeUsageForActivations(activationIds)
                .collectLatest { usageList ->
                    val totalRemaining = usageList.sumOf { it.dataRemaining }
                    val totalCapacity  = usageList.sumOf { it.dataTotal }
                        .takeIf { it > 0.0 } ?: totalPlanGB

                    val remainingPercent = ((totalRemaining / totalCapacity) * 100)
                        .toInt().coerceIn(0, 100)

                    activePlanSummaryRemaining?.text =
                        "${remainingPercent}% remaining  " +
                        "(${formatGB(totalRemaining)} / ${formatGB(totalCapacity)})"
                    activePlanSummaryProgress?.progress = remainingPercent
                }
        }
    }

    private fun extractGB(dataAmount: String): Double {
        val regex = Regex("([0-9]+(?:\\.[0-9]+)?)")
        return regex.find(dataAmount)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }

    private fun formatGB(gb: Double): String =
        if (gb == gb.toLong().toDouble()) "${gb.toLong()} GB" else String.format("%.2f GB", gb)

    private fun showActivePlans(models: List<ActivePlanDisplayModel>) {
        noActivePlansLayout?.visibility    = View.GONE
        activePlansRecyclerView?.visibility = View.VISIBLE
        activePlanCountText?.text = "${models.size} active"
        activePlanAdapter?.submitList(models) {
            activePlansRecyclerView?.scrollToPosition(0)
        }
    }

    private fun showEmptyState() {
        noActivePlansLayout?.visibility    = View.VISIBLE
        activePlansRecyclerView?.visibility = View.GONE
        activePlanSummaryCard?.visibility  = View.GONE
        activePlanCountText?.text = "0 active"
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
                        locationViewModel.saveUserLocation(currentUserId, location.first, location.second, country, "Current Location")
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
                locationViewModel.saveUserLocation(currentUserId, 37.0902, -95.7129, "USA", "Default Location")
            }
        }
    }

    private fun requestLocationPermission(view: View) {
        try {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                PreferenceManager.setLocationPermissionAsked(requireContext())
                detectUserLocation(view)
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PreferenceManager.setLocationPermissionAsked(requireContext())
            if (isAdded && fragmentView != null) detectUserLocation(fragmentView!!)
        }
    }
}
