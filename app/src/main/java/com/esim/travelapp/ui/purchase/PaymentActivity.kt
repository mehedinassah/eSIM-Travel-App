package com.esim.travelapp.ui.purchase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
    private var dataAmount: String = ""
    private var currentUserId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        purchaseId = intent.getIntExtra("purchase_id", 0)
        amount = intent.getDoubleExtra("amount", 0.0)
        dataAmount = intent.getStringExtra("data_amount") ?: "5GB"
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
        val orderSummaryText: TextView = findViewById(R.id.orderSummaryText)
        val amountText: TextView = findViewById(R.id.amountText)
        val cardNumberInput: EditText = findViewById(R.id.cardNumberInput)
        val expiryInput: EditText = findViewById(R.id.expiryInput)
        val cvvInput: EditText = findViewById(R.id.cvvInput)
        val cardHolderInput: EditText = findViewById(R.id.cardHolderInput)
        val payButton: Button = findViewById(R.id.payButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)

        orderSummaryText.text = getString(R.string.plan_label) + ": " + dataAmount
        amountText.text = getString(R.string.amount_paid) + ": ₹" + amount

        payButton.setOnClickListener {
            val cardNumber = cardNumberInput.text.toString().trim()
            val expiry = expiryInput.text.toString().trim()
            val cvv = cvvInput.text.toString().trim()
            val cardHolder = cardHolderInput.text.toString().trim()

            if (cardNumber.isEmpty() || expiry.isEmpty() || cvv.isEmpty() || cardHolder.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transactionRef = "TXN_${System.currentTimeMillis()}"
            paymentViewModel.processPayment(currentUserId, purchaseId, amount, "card", transactionRef)
        }

        cancelButton.setOnClickListener {
            finish()
        }

        paymentViewModel.paymentState.observe(this) { state ->
            when (state) {
                is com.esim.travelapp.presentation.viewmodel.PaymentState.Processing -> {
                    payButton.isEnabled = false
                    payButton.text = "Processing..."
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
                    payButton.isEnabled = true
                    payButton.text = "Pay Now"
                    val failureIntent = Intent(this, PaymentFailureActivity::class.java)
                    failureIntent.putExtra("error_message", state.message)
                    startActivity(failureIntent)
                }
                is com.esim.travelapp.presentation.viewmodel.PaymentState.Error -> {
                    payButton.isEnabled = true
                    payButton.text = "Pay Now"
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
