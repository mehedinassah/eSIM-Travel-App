package com.esim.travelapp.ui.purchase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.repository.PurchaseRepository
import com.esim.travelapp.presentation.viewmodel.PurchaseViewModel
import com.esim.travelapp.presentation.viewmodel.ViewModelFactory
import com.esim.travelapp.ui.BaseActivity
import com.esim.travelapp.utils.PreferenceManager

class PlanDetailsActivity : BaseActivity() {

    private lateinit var purchaseViewModel: PurchaseViewModel
    private var planId: Int = 0
    private var currentUserId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan_details)

        planId = intent.getIntExtra("plan_id", 0)
        currentUserId = PreferenceManager.getUserId(this)

        setupViewModel()
        setupUI()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(this)
        val purchaseRepository = PurchaseRepository(database.purchaseDao())
        val factory = ViewModelFactory(purchaseRepository = purchaseRepository)
        purchaseViewModel = ViewModelProvider(this, factory)[PurchaseViewModel::class.java]
    }

    private fun setupUI() {
        val backButton: ImageView = findViewById(R.id.backButton)
        val countryText: TextView = findViewById(R.id.countryText)
        val planNameText: TextView = findViewById(R.id.planNameText)
        val priceText: TextView = findViewById(R.id.priceText)
        val dataText: TextView = findViewById(R.id.dataText)
        val validityText: TextView = findViewById(R.id.validityText)
        val descriptionText: TextView = findViewById(R.id.descriptionText)
        val purchaseButton: Button = findViewById(R.id.purchaseButton)

        // Load plan details (in a real app, you'd fetch from ViewModel)
        countryText.text = intent.getStringExtra("country") ?: "Unknown"
        planNameText.text = intent.getStringExtra("plan_name") ?: "Plan"
        priceText.text = "₹${intent.getDoubleExtra("price", 0.0)}"
        dataText.text = getString(R.string.data_amount) + ": " + intent.getStringExtra("data_amount")
        validityText.text = getString(R.string.validity) + ": " + intent.getIntExtra("validity_days", 0) + " " + getString(R.string.days_left)
        descriptionText.text = intent.getStringExtra("description") ?: getString(R.string.error)

        backButton.setOnClickListener {
            finish()
        }

        purchaseButton.setOnClickListener {
            purchaseViewModel.createPurchase(currentUserId, planId)
            purchaseViewModel.purchaseState.observe(this) { state ->
                when (state) {
                    is com.esim.travelapp.presentation.viewmodel.PurchaseState.Success -> {
                        val purchaseId = state.purchaseId
                        val amount = intent.getDoubleExtra("price", 0.0)
                        val dataAmount = intent.getStringExtra("data_amount") ?: "5GB"
                        
                        val paymentIntent = Intent(this, PaymentActivity::class.java)
                        paymentIntent.putExtra("purchase_id", purchaseId)
                        paymentIntent.putExtra("amount", amount)
                        paymentIntent.putExtra("data_amount", dataAmount)
                        startActivity(paymentIntent)
                    }
                    is com.esim.travelapp.presentation.viewmodel.PurchaseState.Error -> {
                        Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
}
