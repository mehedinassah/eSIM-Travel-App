package com.esim.travelapp.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.repository.ReferralRepository
import com.esim.travelapp.presentation.viewmodel.ReferralViewModel
import com.esim.travelapp.presentation.viewmodel.ViewModelFactory
import com.esim.travelapp.ui.BaseActivity
import com.esim.travelapp.utils.PreferenceManager

class ReferralActivity : BaseActivity() {

    private lateinit var referralViewModel: ReferralViewModel
    private lateinit var referralsRecyclerView: RecyclerView
    private lateinit var referralCodeTextView: TextView
    private lateinit var referralLinkTextView: TextView
    private lateinit var generateCodeButton: Button
    private lateinit var copyCodeButton: Button
    private lateinit var claimReferralInput: EditText
    private lateinit var claimButton: Button
    private lateinit var benefitsTextView: TextView
    
    private var currentUserId: Int = 0
    private var generatedCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_referral)

        currentUserId = PreferenceManager.getUserId(this)
        setupViewModel()
        initializeViews()
        setupListeners()
        loadReferralData()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(this)
        val referralRepository = ReferralRepository(database.referralDao())
        val factory = ViewModelFactory(referralRepository = referralRepository)
        referralViewModel = ViewModelProvider(this, factory).get(ReferralViewModel::class.java)
    }

    private fun initializeViews() {
        referralsRecyclerView = findViewById(R.id.referralsRecyclerView)
        referralCodeTextView = findViewById(R.id.referralCodeTextView)
        referralLinkTextView = findViewById(R.id.referralLinkTextView)
        generateCodeButton = findViewById(R.id.generateCodeButton)
        copyCodeButton = findViewById(R.id.copyCodeButton)
        claimReferralInput = findViewById(R.id.claimReferralInput)
        claimButton = findViewById(R.id.claimButton)
        benefitsTextView = findViewById(R.id.benefitsTextView)

        referralsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        generateCodeButton.setOnClickListener {
            referralViewModel.generateReferralCode(currentUserId, 10.0)
        }

        copyCodeButton.setOnClickListener {
            if (generatedCode.isNotEmpty()) {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Referral Code", generatedCode)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        claimButton.setOnClickListener {
            val code = claimReferralInput.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "Please enter a referral code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            referralViewModel.claimReferral(code, currentUserId)
        }

        referralViewModel.referralState.observe(this) { state ->
            when (state) {
                is com.esim.travelapp.presentation.viewmodel.ReferralState.CodeGenerated -> {
                    generatedCode = state.code
                    referralCodeTextView.text = "Code: ${state.code}"
                    referralLinkTextView.text = "Link: https://esimtravel.app/join?ref=${state.code}"
                    Toast.makeText(this, "Referral code generated!", Toast.LENGTH_SHORT).show()
                }
                is com.esim.travelapp.presentation.viewmodel.ReferralState.ReferralClaimed -> {
                    Toast.makeText(this, "Referral claimed! Discount: \$${state.discount}", Toast.LENGTH_SHORT).show()
                    claimReferralInput.text.clear()
                    loadReferralData()
                }
                is com.esim.travelapp.presentation.viewmodel.ReferralState.Error -> {
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun loadReferralData() {
        referralViewModel.getUserReferrals(currentUserId).observe(this) { referrals ->
            // Display referrals
        }

        referralViewModel.getTotalBenefits(currentUserId).observe(this) { benefits ->
            benefitsTextView.text = "Total Benefits: \$${benefits ?: 0.0}"
        }

        referralViewModel.getClaimedReferralCount(currentUserId).observe(this) { count ->
            Toast.makeText(this, "Active referrals: $count", Toast.LENGTH_SHORT).show()
        }
    }
}
