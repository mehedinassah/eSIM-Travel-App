package com.esim.travelapp.ui.purchase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.repository.ESIMActivationRepository
import com.esim.travelapp.data.repository.DataUsageRepository
import com.esim.travelapp.presentation.viewmodel.ESIMActivationViewModel
import com.esim.travelapp.presentation.viewmodel.ViewModelFactory
import com.esim.travelapp.ui.BaseActivity

class ESIMActivationActivity : BaseActivity() {

    private lateinit var activationViewModel: ESIMActivationViewModel
    private var purchaseId: Int = 0
    private var dataAmount: String = ""
    private var activationId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_esim_activation)

        purchaseId = intent.getIntExtra("purchase_id", 0)
        dataAmount = intent.getStringExtra("data_amount") ?: "5GB"

        setupViewModel()
        setupUI()
        createActivation()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(this)
        val activationRepository = ESIMActivationRepository(database.esimActivationDao())
        val dataUsageRepository = DataUsageRepository(database.dataUsageDao())
        val factory = ViewModelFactory(activationRepository = activationRepository, dataUsageRepository = dataUsageRepository)
        activationViewModel = ViewModelProvider(this, factory)[ESIMActivationViewModel::class.java]
    }

    private fun setupUI() {
        val backButton: ImageView = findViewById(R.id.backButton)
        val planText: TextView = findViewById(R.id.planText)
        val statusText: TextView = findViewById(R.id.statusText)
        val activateButton: Button = findViewById(R.id.activateButton)
        val backToDashboardButton: Button = findViewById(R.id.backToDashboardButton)

        planText.text = getString(R.string.plan_label) + ": " + dataAmount

        backButton.setOnClickListener {
            finish()
        }

        activationViewModel.activationState.observe(this) { state ->
            when (state) {
                is com.esim.travelapp.presentation.viewmodel.ActivationState.Created -> {
                    activationId = state.activationId
                    val qrCodeUrl = state.qrCode
                    val iccidValue = state.iccid
                    
                    val qrImageView: ImageView = findViewById(R.id.qrCodeImageView)
                    val iccidText: TextView = findViewById(R.id.iccidText)
                    
                    Glide.with(this)
                        .load(qrCodeUrl)
                        .placeholder(R.drawable.ic_menu)
                        .into(qrImageView)
                    
                    iccidText.text = getString(R.string.iccid_label) + ": " + iccidValue
                    statusText.text = getString(R.string.pending_activation)
                    
                    activateButton.isEnabled = true
                }
                is com.esim.travelapp.presentation.viewmodel.ActivationState.Activated -> {
                    statusText.text = getString(R.string.status_activated)
                    activateButton.isEnabled = false
                    activateButton.text = getString(R.string.status_activated)
                    Toast.makeText(this, getString(R.string.esim_activated_successfully), Toast.LENGTH_SHORT).show()
                }
                is com.esim.travelapp.presentation.viewmodel.ActivationState.Error -> {
                    Toast.makeText(this, getString(R.string.error) + ": " + state.message, Toast.LENGTH_SHORT).show()
                    statusText.text = getString(R.string.status_failed)
                }
                else -> {}
            }
        }

        activateButton.setOnClickListener {
            if (activationId > 0) {
                activationViewModel.activateESIM(activationId)
            }
        }

        backToDashboardButton.setOnClickListener {
            val intent = Intent(this, com.esim.travelapp.ui.main.MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun createActivation() {
        activationViewModel.createActivation(purchaseId, dataAmount)
    }
}
