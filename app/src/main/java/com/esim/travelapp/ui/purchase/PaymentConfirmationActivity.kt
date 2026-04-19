package com.esim.travelapp.ui.purchase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.esim.travelapp.R
import com.esim.travelapp.ui.BaseActivity

class PaymentConfirmationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_confirmation)

        val paymentId = intent.getIntExtra("payment_id", 0)
        val purchaseId = intent.getIntExtra("purchase_id", 0)
        val amount = intent.getDoubleExtra("amount", 0.0)
        val dataAmount = intent.getStringExtra("data_amount") ?: "5GB"

        setupUI(paymentId, amount, dataAmount, purchaseId)
    }

    private fun setupUI(paymentId: Int, amount: Double, dataAmount: String, purchaseId: Int) {
        val orderIdText: TextView = findViewById(R.id.orderIdText)
        val amountText: TextView = findViewById(R.id.amountText)
        val planText: TextView = findViewById(R.id.planText)
        val continueButton: Button = findViewById(R.id.continueButton)

        orderIdText.text = getString(R.string.order_id) + ": #" + paymentId
        amountText.text = getString(R.string.amount_paid) + ": ₹" + amount
        planText.text = getString(R.string.plan_label) + ": " + dataAmount

        continueButton.setOnClickListener {
            val activationIntent = Intent(this, ESIMActivationActivity::class.java)
            activationIntent.putExtra("purchase_id", purchaseId)
            activationIntent.putExtra("data_amount", dataAmount)
            startActivity(activationIntent)
            finish()
        }
    }
}
