package com.esim.travelapp.ui.purchase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.esim.travelapp.R
import com.esim.travelapp.ui.BaseActivity

class PaymentFailureActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_failure)

        val errorMessage = intent.getStringExtra("error_message") ?: getString(R.string.payment_failed)

        setupUI(errorMessage)
    }

    private fun setupUI(errorMessage: String) {
        val errorText: TextView = findViewById(R.id.errorText)
        val retryButton: Button = findViewById(R.id.retryButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)

        errorText.text = errorMessage

        retryButton.setOnClickListener {
            finish()
        }

        cancelButton.setOnClickListener {
            val intent = Intent(this, com.esim.travelapp.ui.main.MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}
