package com.esim.travelapp.ui.profile

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.repository.PurchaseRepository
import com.esim.travelapp.presentation.viewmodel.PurchaseViewModel
import com.esim.travelapp.presentation.viewmodel.ViewModelFactory
import com.esim.travelapp.ui.BaseActivity
import com.esim.travelapp.ui.adapter.PurchaseHistoryAdapter
import com.esim.travelapp.utils.PreferenceManager

class PurchaseHistoryActivity : BaseActivity() {

    private lateinit var purchaseViewModel: PurchaseViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var purchaseAdapter: PurchaseHistoryAdapter
    private var currentUserId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_history)

        currentUserId = PreferenceManager.getUserId(this)

        setupViewModel()
        setupUI()
        loadPurchases()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(this)
        val purchaseRepository = PurchaseRepository(database.purchaseDao())
        val factory = ViewModelFactory(purchaseRepository = purchaseRepository)
        purchaseViewModel = ViewModelProvider(this, factory)[PurchaseViewModel::class.java]
    }

    private fun setupUI() {
        val backButton: ImageView = findViewById(R.id.backButton)
        recyclerView = findViewById(R.id.purchaseRecyclerView)
        emptyText = findViewById(R.id.emptyText)

        backButton.setOnClickListener {
            finish()
        }

        purchaseAdapter = PurchaseHistoryAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = purchaseAdapter
    }

    private fun loadPurchases() {
        purchaseViewModel.getUserPurchases(currentUserId).asLiveData().observe(this) { purchases ->
            if (purchases.isEmpty()) {
                emptyText.visibility = TextView.VISIBLE
                recyclerView.visibility = RecyclerView.GONE
            } else {
                emptyText.visibility = TextView.GONE
                recyclerView.visibility = RecyclerView.VISIBLE
                purchaseAdapter.submitList(purchases)
            }
        }
    }
}
