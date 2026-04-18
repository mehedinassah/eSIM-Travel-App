package com.esim.travelapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.repository.ESIMPlanRepository
import com.esim.travelapp.presentation.viewmodel.ESIMPlanViewModel
import com.esim.travelapp.presentation.viewmodel.ViewModelFactory
import com.esim.travelapp.ui.adapter.PlanAdapter

class StorefrontFragment : Fragment() {

    private lateinit var planViewModel: ESIMPlanViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var emptyText: TextView
    private lateinit var planAdapter: PlanAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_storefront, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupViewModel()
        setupRecyclerView()
        loadPlans()
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.plansRecyclerView)
        searchInput = view.findViewById(R.id.searchInput)
        emptyText = view.findViewById(R.id.emptyText)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val planRepository = ESIMPlanRepository(database.esimPlanDao())
        val factory = ViewModelFactory(planRepository = planRepository)
        planViewModel = ViewModelProvider(this, factory).get(ESIMPlanViewModel::class.java)
    }

    private fun setupRecyclerView() {
        planAdapter = PlanAdapter { plan ->
            Toast.makeText(requireContext(), "Selected: ${plan.planName}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = planAdapter
    }

    private fun loadPlans() {
        planViewModel.plans.asLiveData().observe(viewLifecycleOwner) { plans ->
            if (plans.isEmpty()) {
                emptyText.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyText.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                planAdapter.submitList(plans)
            }
        }
    }
}
