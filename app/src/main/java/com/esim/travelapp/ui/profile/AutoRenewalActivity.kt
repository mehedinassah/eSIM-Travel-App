package com.esim.travelapp.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.repository.AutoRenewalRepository
import com.esim.travelapp.data.repository.ESIMPlanRepository
import com.esim.travelapp.presentation.viewmodel.AutoRenewalViewModel
import com.esim.travelapp.presentation.viewmodel.ESIMPlanViewModel
import com.esim.travelapp.presentation.viewmodel.ViewModelFactory
import com.esim.travelapp.ui.BaseActivity
import com.esim.travelapp.utils.PreferenceManager

class AutoRenewalActivity : BaseActivity() {

    private lateinit var autoRenewalViewModel: AutoRenewalViewModel
    private lateinit var renewalsRecyclerView: RecyclerView
    private lateinit var autoRenewSwitch: Switch
    private lateinit var planSpinner: android.widget.Spinner
    private lateinit var thresholdSeekBar: SeekBar
    private lateinit var thresholdTextView: TextView
    private lateinit var enableButton: Button
    
    private var currentUserId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_renewal)

        currentUserId = PreferenceManager.getUserId(this)
        setupViewModel()
        initializeViews()
        setupListeners()
        loadAutoRenewals()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(this)
        val autoRenewalRepository = AutoRenewalRepository(database.autoRenewalDao())
        val factory = ViewModelFactory(autoRenewalRepository = autoRenewalRepository)
        autoRenewalViewModel = ViewModelProvider(this, factory).get(AutoRenewalViewModel::class.java)
    }

    private fun initializeViews() {
        renewalsRecyclerView = findViewById(R.id.renewalsRecyclerView)
        autoRenewSwitch = findViewById(R.id.autoRenewSwitch)
        planSpinner = findViewById(R.id.planSpinner)
        thresholdSeekBar = findViewById(R.id.thresholdSeekBar)
        thresholdTextView = findViewById(R.id.thresholdTextView)
        enableButton = findViewById(R.id.enableButton)

        renewalsRecyclerView.layoutManager = LinearLayoutManager(this)

        thresholdSeekBar.max = 100
        thresholdSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                thresholdTextView.text = "Renew at ${progress}% data used"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupListeners() {
        enableButton.setOnClickListener {
            val threshold = thresholdSeekBar.progress.toDouble() / 100.0 * 100.0
            autoRenewalViewModel.enableAutoRenewal(currentUserId, 1, threshold) // planId = 1 for demo
        }

        autoRenewalViewModel.renewalState.observe(this) { state ->
            when (state) {
                is com.esim.travelapp.presentation.viewmodel.RenewalState.Success -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    loadAutoRenewals()
                }
                is com.esim.travelapp.presentation.viewmodel.RenewalState.Error -> {
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun loadAutoRenewals() {
        autoRenewalViewModel.getUserAutoRenewals(currentUserId).observe(this) { renewals ->
            Toast.makeText(this, "Loaded ${renewals.size} auto-renewals", Toast.LENGTH_SHORT).show()
        }
    }
}
