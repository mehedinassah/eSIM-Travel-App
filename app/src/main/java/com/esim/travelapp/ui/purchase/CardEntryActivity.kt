package com.esim.travelapp.ui.purchase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.esim.travelapp.R
import com.esim.travelapp.ui.BaseActivity
import com.stripe.android.view.CardInputWidget

/**
 * Dedicated activity for entering credit card details
 * User enters card number, expiry, and CVC on this separate screen
 */
class CardEntryActivity : BaseActivity() {

    private lateinit var cardInputWidget: CardInputWidget
    private lateinit var cardTypeText: TextView
    private lateinit var continueButton: Button
    private lateinit var backButton: Button
    
    // Purchase details passed from PaymentActivity
    private var purchaseId: Int = 0
    private var amount: Double = 0.0
    private var planName: String = ""
    private var dataAmount: String = ""
    private var validity: String = ""
    private var country: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_entry)

        // Get purchase details from intent
        purchaseId = intent.getIntExtra("purchase_id", 0)
        amount = intent.getDoubleExtra("amount", 0.0)
        planName = intent.getStringExtra("plan_name") ?: "eSIM Plan"
        dataAmount = intent.getStringExtra("data_amount") ?: "5GB"
        validity = intent.getStringExtra("validity") ?: "30 days"
        country = intent.getStringExtra("country") ?: "USA"

        setupUI()
    }

    private fun setupUI() {
        cardInputWidget = findViewById(R.id.cardInputWidget)
        cardTypeText = findViewById(R.id.cardTypeText)
        continueButton = findViewById(R.id.continueButton)
        backButton = findViewById(R.id.backButton)

        val cancelButton = findViewById<Button>(R.id.cancelButton)
        
        // Back button returns to payment method selection
        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Cancel button also returns
        cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Continue button validates and proceeds to payment
        continueButton.setOnClickListener {
            val cardParams = cardInputWidget.cardParams

            if (cardParams == null) {
                Toast.makeText(this, "Please enter valid card details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Card is valid, return to PaymentActivity with confirmation
            val intent = Intent()
            intent.putExtra("card_ready", true)
            setResult(RESULT_OK, intent)
            finish()
        }
        // Monitor card input listener code removed - Stripe API incompatibility
    }
}


