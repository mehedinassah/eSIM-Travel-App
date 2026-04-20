package com.esim.travelapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.repository.ESIMPlanRepository
import com.esim.travelapp.data.repository.LocationRepository
import com.esim.travelapp.data.repository.WishlistRepository
import com.esim.travelapp.presentation.viewmodel.ESIMPlanViewModel
import com.esim.travelapp.presentation.viewmodel.LocationViewModel
import com.esim.travelapp.presentation.viewmodel.WishlistViewModel
import com.esim.travelapp.presentation.viewmodel.ViewModelFactory
import com.esim.travelapp.ui.adapter.PlanAdapter
import com.esim.travelapp.ui.purchase.PlanDetailsActivity
import com.esim.travelapp.utils.LocationManager
import com.esim.travelapp.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StorefrontEnhancedFragment : Fragment() {

    private lateinit var planViewModel: ESIMPlanViewModel
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var wishlistViewModel: WishlistViewModel
    private lateinit var locationManager: LocationManager
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var locationText: TextView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var planAdapter: PlanAdapter
    private lateinit var countrySpinner: Spinner
    private lateinit var priceSeekBar: SeekBar
    private lateinit var dataSeekBar: SeekBar
    private lateinit var priceRangeText: TextView
    private lateinit var dataRangeText: TextView
    private lateinit var clearFiltersButton: Button
    
    private var currentUserId: Int = 0
    private var selectedCountry: String = ""
    private var maxPrice: Double = 50.0
    private var minDataGB: Double = 1.0
    
    private val allPlans = mutableListOf<com.esim.travelapp.data.local.entity.ESIMPlanEntity>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_storefront_enhanced_pro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = PreferenceManager.getUserId(requireContext())
        locationManager = LocationManager(requireContext())

        initializeViews(view)
        setupViewModels()
        setupRecyclerView()
        loadPlans()
        requestLocationAndShowRecommendations()
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.plansRecyclerView)
        searchInput = view.findViewById(R.id.searchEditText)
        locationText = view.findViewById(R.id.locationText)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        countrySpinner = view.findViewById(R.id.countrySpinner)
        priceSeekBar = view.findViewById(R.id.priceSeekBar)
        dataSeekBar = view.findViewById(R.id.dataSeekBar)
        priceRangeText = view.findViewById(R.id.priceRangeText)
        dataRangeText = view.findViewById(R.id.dataRangeText)
        clearFiltersButton = view.findViewById(R.id.clearFiltersButton)

        // Setup country spinner
        val countries = listOf("All", "USA", "UK", "Canada", "France", "Germany", "Japan", "Australia", "India")
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, countries)
        countrySpinner.adapter = adapter

        // Setup seek bars for price filtering
        priceSeekBar.max = 50
        priceSeekBar.progress = 50
        priceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                maxPrice = progress.toDouble()
                priceRangeText.text = "$$0 - $${maxPrice.toInt()}"
                applyFilters()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Setup seek bar for data filtering
        dataSeekBar.max = 30
        dataSeekBar.progress = 1
        dataSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                minDataGB = progress.toDouble()
                dataRangeText.text = "${minDataGB.toInt()} GB - 30 GB"
                applyFilters()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        countrySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCountry = if (position == 0) "" else countries[position]
                applyFilters()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        clearFiltersButton.setOnClickListener {
            selectedCountry = ""
            maxPrice = 50.0
            minDataGB = 1.0
            searchInput.text.clear()
            priceSeekBar.progress = 50
            dataSeekBar.progress = 1
            countrySpinner.setSelection(0)
            priceRangeText.text = "$$0 - $50"
            dataRangeText.text = "1 GB - 30 GB"
            applyFilters()
            Toast.makeText(requireContext(), "Filters cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewModels() {
        val database = AppDatabase.getInstance(requireContext())
        
        val planRepository = ESIMPlanRepository(database.esimPlanDao())
        val locationRepository = LocationRepository(database.locationDao())
        val wishlistRepository = WishlistRepository(database.wishlistDao())
        
        val factory = ViewModelFactory(
            planRepository = planRepository,
            locationRepository = locationRepository,
            wishlistRepository = wishlistRepository
        )
        
        planViewModel = ViewModelProvider(this, factory).get(ESIMPlanViewModel::class.java)
        locationViewModel = ViewModelProvider(this, factory).get(LocationViewModel::class.java)
        wishlistViewModel = ViewModelProvider(this, factory).get(WishlistViewModel::class.java)
    }

    private fun setupRecyclerView() {
        planAdapter = PlanAdapter { plan ->
            val intent = Intent(requireContext(), PlanDetailsActivity::class.java)
            intent.putExtra("plan_id", plan.id)
            intent.putExtra("country", plan.country)
            intent.putExtra("plan_name", plan.planName)
            intent.putExtra("data_amount", plan.dataAmount)
            intent.putExtra("validity_days", plan.validityDays)
            intent.putExtra("price", plan.price)
            intent.putExtra("description", plan.description)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = planAdapter
    }

    private fun loadPlans() {
        planViewModel.plans.asLiveData().observe(viewLifecycleOwner) { plans ->
            allPlans.clear()
            allPlans.addAll(plans)
            applyFilters()
        }
    }

    private fun applyFilters() {
        val query = searchInput.text.toString().lowercase()
        
        val filtered = allPlans.filter { plan ->
            val matchesSearch = plan.planName.lowercase().contains(query) || 
                               plan.country.lowercase().contains(query) ||
                               plan.description.lowercase().contains(query)
            val matchesCountry = selectedCountry.isEmpty() || plan.country == selectedCountry
            val matchesPrice = plan.price <= maxPrice
            val dataGb = plan.dataAmount.replace("GB", "").toDoubleOrNull() ?: 0.0
            val matchesData = dataGb >= minDataGB
            
            matchesSearch && matchesCountry && matchesPrice && matchesData
        }

        if (filtered.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            planAdapter.submitList(filtered)
        }
    }

    private fun requestLocationAndShowRecommendations() {
        locationText.text = "Detecting your location..."
        
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
                        
                        // Update location text display
                        locationText.text = "🌍 $country"
                        
                        // Auto-select user's country in spinner
                        val countryList = listOf("All", "USA", "UK", "Canada", "France", "Germany", "Japan", "Australia", "India")
                        val index = countryList.indexOf(country)
                        if (index > 0) {
                            countrySpinner.setSelection(index)
                        }
                        Toast.makeText(requireContext(), "Showing plans for $country", Toast.LENGTH_SHORT).show()
                    } else {
                        locationText.text = "Location not recognized"
                    }
                } else {
                    locationText.text = "Enable location to see personalized plans"
                }
            } else {
                locationText.text = "Enable location to see personalized plans"
            }
        }
    }
}
