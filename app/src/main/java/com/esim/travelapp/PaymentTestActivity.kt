package com.esim.travelapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.esim.travelapp.utils.PaymentManager

/**
 * Test activity for payment flow testing
 * Use Stripe test card numbers:
 * - Visa: 4242 4242 4242 4242
 * - Mastercard: 5555 5555 5555 4444
 * - Amex: 3782 822463 10005
 * - Declined: 4000 0000 0000 0002
 *
 * Use any future expiry date (MM/YY) and any 3-4 digit CVC
 */
class PaymentTestActivity : AppCompatActivity() {

    private lateinit var paymentManager: PaymentManager
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_test)

        paymentManager = PaymentManager(this)
        setupUI()
    }

    private fun setupUI() {
        statusText = findViewById(R.id.statusText)
        val cardNumberInput: EditText = findViewById(R.id.cardNumberInput)
        val expiryMonthInput: EditText = findViewById(R.id.expiryMonthInput)
        val expiryYearInput: EditText = findViewById(R.id.expiryYearInput)
        val cvcInput: EditText = findViewById(R.id.cvcInput)
        val amountInput: EditText = findViewById(R.id.amountInput)

        // Pre-fill with test card
        cardNumberInput.setText("4242424242424242")
        expiryMonthInput.setText("12")
        expiryYearInput.setText("25")
        cvcInput.setText("123")
        amountInput.setText("29.99")

        findViewById<Button>(R.id.testCardPaymentButton).setOnClickListener {
            val cardNumber = cardNumberInput.text.toString()
            val expiryMonth = expiryMonthInput.text.toString().toIntOrNull() ?: 0
            val expiryYear = expiryYearInput.text.toString().toIntOrNull() ?: 0
            val cvc = cvcInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0

            if (cardNumber.isEmpty() || expiryMonth == 0 || expiryYear == 0 || cvc.isEmpty() || amount == 0.0) {
                updateStatus("❌ Please fill in all fields", true)
                return@setOnClickListener
            }

            testCardPayment(cardNumber, expiryMonth, expiryYear, cvc, amount)
        }

        findViewById<Button>(R.id.testGooglePayButton).setOnClickListener {
            testGooglePay()
        }

        findViewById<Button>(R.id.testBankTransferButton).setOnClickListener {
            testBankTransfer()
        }

        findViewById<Button>(R.id.testDeclinedCardButton).setOnClickListener {
            // Test with declined card
            testCardPayment("4000000000000002", 12, 25, "123", 29.99)
        }
    }

    private fun testCardPayment(cardNumber: String, expiryMonth: Int, expiryYear: Int, cvc: String, amount: Double) {
        updateStatus("⏳ Processing card payment...", false)

        paymentManager.processCardPayment(
            cardNumber = cardNumber,
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            cvc = cvc,
            amount = amount,
            currency = "USD",
            onSuccess = { transactionId ->
                updateStatus("✅ Payment successful!\nTransaction ID: $transactionId", true)
            },
            onError = { error ->
                updateStatus("❌ Payment failed: $error", true)
            }
        )
    }

    private fun testGooglePay() {
        updateStatus("⏳ Processing Google Pay payment...", false)

        paymentManager.processDigitalWallet(
            walletType = "google_pay",
            amount = 29.99,
            currency = "USD",
            onSuccess = { transactionId ->
                updateStatus("✅ Google Pay payment successful!\nTransaction ID: $transactionId", true)
            },
            onError = { error ->
                updateStatus("❌ Google Pay failed: $error", true)
            }
        )
    }

    private fun testBankTransfer() {
        updateStatus("⏳ Setting up bank transfer...", false)

        paymentManager.processBankTransfer(
            amount = 29.99,
            currency = "USD",
            onSuccess = { transactionId ->
                updateStatus("✅ Bank transfer reference created!\nReference: $transactionId", true)
            },
            onError = { error ->
                updateStatus("❌ Bank transfer failed: $error", true)
            }
        )
    }

    private fun updateStatus(message: String, isComplete: Boolean) {
        runOnUiThread {
            statusText.text = message
            statusText.setTextColor(
                if (message.startsWith("✅")) {
                    android.graphics.Color.GREEN
                } else if (message.startsWith("❌")) {
                    android.graphics.Color.RED
                } else {
                    android.graphics.Color.BLUE
                }
            )
        }
    }
}
