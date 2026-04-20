package com.esim.travelapp.ui.purchase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.repository.PaymentRepository
import com.esim.travelapp.presentation.viewmodel.PaymentViewModel
import com.esim.travelapp.presentation.viewmodel.ViewModelFactory
import com.esim.travelapp.ui.BaseActivity
import com.esim.travelapp.utils.PreferenceManager

class PaymentActivity : BaseActivity() {

    private lateinit var paymentViewModel: PaymentViewModel
    private var purchaseId: Int = 0
    private var amount: Double = 0.0
    private var planName: String = ""
    private var dataAmount: String = ""
    private var validity: String = ""
    private var country: String = ""
    private var currentUserId: Int = 0
    
    // UI Elements
    private lateinit var planNameText: TextView
    private lateinit var dataText: TextView
    private lateinit var validityText: TextView
    private lateinit var priceText: TextView
    private lateinit var countryText: TextView
    private lateinit var paymentMethodGroup: RadioGroup
    private lateinit var closeButton: Button
    private lateinit var cancelButton: Button
    private lateinit var payNowButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_professional)

        purchaseId = intent.getIntExtra("purchase_id", 0)
        amount = intent.getDoubleExtra("amount", 0.0)
        planName = intent.getStringExtra("plan_name") ?: "eSIM Plan"
        dataAmount = intent.getStringExtra("data_amount") ?: "5GB"
        validity = intent.getStringExtra("validity") ?: "30 days"
        country = intent.getStringExtra("country") ?: "USA"
        currentUserId = PreferenceManager.getUserId(this)

        setupViewModel()
        setupUI()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(this)
        val paymentRepository = PaymentRepository(database.paymentDao())
        val factory = ViewModelFactory(paymentRepository = paymentRepository)
        paymentViewModel = ViewModelProvider(this, factory)[PaymentViewModel::class.java]
    }

    private fun setupUI() {
        planNameText = findViewById(R.id.planNameText)
        dataText = findViewById(R.id.dataText)
        validityText = findViewById(R.id.validityText)
        priceText = findViewById(R.id.priceText)
        countryText = findViewById(R.id.countryText)
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup)
        closeButton = findViewById(R.id.closeButton)
        cancelButton = findViewById(R.id.cancelButton)
        payNowButton = findViewById(R.id.payNowButton)

        // Display plan details
        planNameText.text = planName
        dataText.text = dataAmount
        validityText.text = validity
        priceText.text = "$$amount"
        countryText.text = country

        // Close button
        closeButton.setOnClickListener {
            finish()
        }

        // Cancel button
        cancelButton.setOnClickListener {
            finish()
        }

        payNowButton.setOnClickListener {
            val selectedPaymentMethodId = paymentMethodGroup.checkedRadioButtonId
            
            if (selectedPaymentMethodId == -1) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val paymentMethod = when (selectedPaymentMethodId) {
                R.id.creditCardRadio -> "credit_card"
                R.id.googlePayRadio -> "google_pay"
                R.id.bankTransferRadio -> "bank_transfer"
                else -> "unknown"
            }

            processPayment(paymentMethod)
        }
    }

    private fun processPayment(paymentMethod: String) {
        val transactionRef = "TXN_${System.currentTimeMillis()}"
        
        payNowButton.isEnabled = false
        payNowButton.text = "Processing..."

        // Show different process based on payment method
        when (paymentMethod) {
            "credit_card" -> {
                // Process credit card directly
                paymentViewModel.processPayment(currentUserId, purchaseId, amount, "card", transactionRef)
            }
            "google_pay" -> {
                Toast.makeText(this, "Google Pay integration coming soon", Toast.LENGTH_SHORT).show()
                payNowButton.isEnabled = true
                payNowButton.text = "Proceed"
            }
            "bank_transfer" -> {
                Toast.makeText(this, "Redirecting to bank transfer", Toast.LENGTH_SHORT).show()
                payNowButton.isEnabled = true
                payNowButton.text = "Proceed"
            }
        }

        paymentViewModel.paymentState.observe(this) { state ->
            when (state) {
                is com.esim.travelapp.presentation.viewmodel.PaymentState.Processing -> {
                    payNowButton.isEnabled = false
                    payNowButton.text = "Processing..."
                }
                is com.esim.travelapp.presentation.viewmodel.PaymentState.Success -> {
                    val paymentId = state.paymentId
                    val confirmationIntent = Intent(this, PaymentConfirmationActivity::class.java)
                    confirmationIntent.putExtra("payment_id", paymentId)
                    confirmationIntent.putExtra("purchase_id", purchaseId)
                    confirmationIntent.putExtra("amount", amount)
                    confirmationIntent.putExtra("data_amount", dataAmount)
                    startActivity(confirmationIntent)
                    finish()
                }
                is com.esim.travelapp.presentation.viewmodel.PaymentState.Failed -> {
                    payNowButton.isEnabled = true
                    payNowButton.text = "Proceed"
                    val failureIntent = Intent(this, PaymentFailureActivity::class.java)
                    failureIntent.putExtra("error_message", state.message)
                    startActivity(failureIntent)
                }
                is com.esim.travelapp.presentation.viewmodel.PaymentState.Error -> {
                    payNowButton.isEnabled = true
                    payNowButton.text = "Proceed"
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
