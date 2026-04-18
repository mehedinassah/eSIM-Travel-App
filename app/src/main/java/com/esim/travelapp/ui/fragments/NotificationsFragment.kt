package com.esim.travelapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.repository.NotificationRepository
import com.esim.travelapp.presentation.viewmodel.NotificationViewModel
import com.esim.travelapp.presentation.viewmodel.ViewModelFactory
import com.esim.travelapp.ui.adapter.NotificationAdapter
import com.esim.travelapp.utils.PreferenceManager

class NotificationsFragment : Fragment() {

    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var clearAllButton: Button
    private lateinit var notificationAdapter: NotificationAdapter
    private var currentUserId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = PreferenceManager.getUserId(requireContext())

        initializeViews(view)
        setupViewModel()
        setupRecyclerView()
        loadNotifications()
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.notificationsRecyclerView)
        emptyText = view.findViewById(R.id.emptyText)
        clearAllButton = view.findViewById(R.id.clearAllButton)

        clearAllButton.setOnClickListener {
            Toast.makeText(requireContext(), "Notifications cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val notificationRepository = NotificationRepository(database.notificationDao())
        val factory = ViewModelFactory(notificationRepository = notificationRepository)
        notificationViewModel = ViewModelProvider(this, factory).get(NotificationViewModel::class.java)
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = notificationAdapter
    }

    private fun loadNotifications() {
        notificationViewModel.getUserNotifications(currentUserId).asLiveData().observe(viewLifecycleOwner) { notifications ->
            if (notifications.isEmpty()) {
                emptyText.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyText.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                notificationAdapter.submitList(notifications)
            }
        }
    }
}
