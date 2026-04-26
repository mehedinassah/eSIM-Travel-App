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

    // Summary card views kept as fields for live updates
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

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        fragmentView?.let { if (isAdded) detectUserLocation(it) }
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
        val factory = ViewModelFactory(
            purchaseRepository = purchaseRepository,
            locationRepository = locationRepository
        )
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
        activePlansRecyclerView   = view.findViewById(R.id.activePlansRecyclerView)
        activePlanCountText       = view.findViewById(R.id.activePlanCountText)
        noActivePlansLayout       = view.findViewById(R.id.noActivePlansLayout)
        activePlanSummaryCard     = view.findViewById(R.id.activePlanSummaryCard)
        activePlanSummaryName     = view.findViewById(R.id.activePlanSummaryName)
        activePlanSummaryData     = view.findViewById(R.id.activePlanSummaryData)
        activePlanSummaryRemaining = view.findViewById(R.id.activePlanSummaryRemaining)
        activePlanSummaryProgress = view.findViewById(R.id.activePlanSummaryProgress)
        activePlanSummaryValidity  = view.findViewById(R.id.activePlanSummaryValidity)

        // Default: show empty state
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
                // Show ALL completed purchases — no activation gate
                val completedPurchases = purchases.filter { it.status == "completed" }

                if (completedPurchases.isEmpty()) {
                    showEmptyState()
                    return@observe
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    val displayModels = withContext(Dispatchers.IO) {
                        completedPurchases.mapNotNull { purchase ->
                            try {
                                val plan = database.esimPlanDao().getPlanById(purchase.planId)
                                    ?: return@mapNotNull null

                                // Try to get activation & usage — but don't gate on it
                                val activation = database.esimActivationDao()
                                    .getActivationByPurchaseId(purchase.id)
                                val usage = activation?.let {
                                    database.dataUsageDao().getUsageByActivationId(it.id)
                                }

                                ActivePlanDisplayModel(purchase, plan, usage)
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }

                    if (displayModels.isEmpty()) {
                        showEmptyState()
                        return@launch
                    }

                    // Show the list
                    showActivePlans(displayModels)

                    // Live-observe usage for the first plan's summary card
                    val firstActivation = withContext(Dispatchers.IO) {
                        database.esimActivationDao()
                            .getActivationByPurchaseId(displayModels.first().purchase.id)
                    }

                    if (firstActivation != null) {
                        startLiveUsageObserver(firstActivation.id, displayModels.first())
                    } else {
                        // No activation record yet — just show plan info without usage
                        val first = displayModels.first()
                        activePlanSummaryName?.text     = first.plan.planName
                        activePlanSummaryData?.text     = "Data: ${first.plan.dataAmount}"
                        activePlanSummaryRemaining?.text = "100% remaining (${first.plan.dataAmount})"
                        activePlanSummaryProgress?.progress = 100
                        activePlanSummaryValidity?.text = "Validity: ${first.plan.validityDays} days"
                        activePlanSummaryCard?.visibility = View.VISIBLE
                    }
                }
            }
    }

    /**
     * Observes DataUsageEntity via Flow so the summary card updates in real-time
     * every second as DataUsageService writes to the DB.
     */
    private fun startLiveUsageObserver(
        activationId: Int,
        primaryModel: ActivePlanDisplayModel
    ) {
        usageObserverJob?.cancel()
        usageObserverJob = viewLifecycleOwner.lifecycleScope.launch {
            database.dataUsageDao()
                .observeUsageByActivationId(activationId)
                .collectLatest { usage ->
                    val plan = primaryModel.plan
                    val remainingPercent = if (usage != null && usage.dataTotal > 0) {
                        ((usage.dataRemaining / usage.dataTotal) * 100).toInt().coerceIn(0, 100)
                    } else 100

                    activePlanSummaryName?.text = plan.planName
                    activePlanSummaryData?.text = "Data: ${plan.dataAmount}"
                    activePlanSummaryRemaining?.text = if (usage != null) {
                        "${remainingPercent}% remaining  " +
                        "(${String.format("%.2f", usage.dataRemaining)} / " +
                        "${String.format("%.2f", usage.dataTotal)} GB)"
                    } else {
                        "100% remaining (${plan.dataAmount})"
                    }
                    activePlanSummaryProgress?.progress = remainingPercent
                    activePlanSummaryValidity?.text = "Validity: ${plan.validityDays} days"
                    activePlanSummaryCard?.visibility = View.VISIBLE
                }
        }
    }

    private fun showActivePlans(models: List<ActivePlanDisplayModel>) {
        noActivePlansLayout?.visibility   = View.GONE
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
