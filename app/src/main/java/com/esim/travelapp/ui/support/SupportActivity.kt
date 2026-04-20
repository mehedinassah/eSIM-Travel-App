package com.esim.travelapp.ui.support

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esim.travelapp.R
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.local.entity.SupportMessageEntity
import com.esim.travelapp.data.local.entity.SupportTicketEntity
import com.esim.travelapp.data.repository.SupportRepository
import com.esim.travelapp.presentation.viewmodel.SupportViewModel
import com.esim.travelapp.presentation.viewmodel.ViewModelFactory
import com.esim.travelapp.ui.BaseActivity
import com.esim.travelapp.ui.adapter.SupportMessageAdapter
import com.esim.travelapp.utils.PreferenceManager

class SupportActivity : BaseActivity() {

    private lateinit var supportViewModel: SupportViewModel
    private lateinit var ticketsRecyclerView: RecyclerView
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var subjectInput: EditText
    private lateinit var messageInput: EditText
    private lateinit var prioritySpinner: Spinner
    private lateinit var createTicketButton: Button
    private lateinit var sendMessageButton: Button
    
    private var currentUserId: Int = 0
    private var selectedTicketId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)

        currentUserId = PreferenceManager.getUserId(this)
        setupViewModel()
        initializeViews()
        setupListeners()
        loadTickets()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(this)
        val supportRepository = SupportRepository(database.supportTicketDao(), database.supportMessageDao())
        val factory = ViewModelFactory(supportRepository = supportRepository)
        supportViewModel = ViewModelProvider(this, factory)[SupportViewModel::class.java]
    }

    private fun initializeViews() {
        ticketsRecyclerView = findViewById(R.id.ticketsRecyclerView)
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        subjectInput = findViewById(R.id.subjectInput)
        messageInput = findViewById(R.id.messageInput)
        prioritySpinner = findViewById(R.id.prioritySpinner)
        createTicketButton = findViewById(R.id.createTicketButton)
        sendMessageButton = findViewById(R.id.sendMessageButton)

        val priorities = listOf("Low", "Normal", "High")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        prioritySpinner.adapter = adapter

        ticketsRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        createTicketButton.setOnClickListener {
            val subject = subjectInput.text.toString().trim()
            val message = messageInput.text.toString().trim()
            val priority = prioritySpinner.selectedItem.toString().lowercase()

            if (subject.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            supportViewModel.createTicket(currentUserId, subject, message, priority)
        }

        sendMessageButton.setOnClickListener {
            if (selectedTicketId == 0) {
                Toast.makeText(this, "Please select a ticket first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val message = messageInput.text.toString().trim()
            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            supportViewModel.sendMessage(selectedTicketId, currentUserId, message)
            messageInput.text.clear()
        }

        supportViewModel.ticketState.observe(this) { state ->
            when (state) {
                is com.esim.travelapp.presentation.viewmodel.SupportState.TicketCreated -> {
                    Toast.makeText(this, "Ticket created: #${state.ticketId}", Toast.LENGTH_SHORT).show()
                    subjectInput.text.clear()
                    messageInput.text.clear()
                    selectedTicketId = state.ticketId
                    loadTickets()
                }
                is com.esim.travelapp.presentation.viewmodel.SupportState.Error -> {
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        supportViewModel.messageState.observe(this) { state ->
            when (state) {
                is com.esim.travelapp.presentation.viewmodel.SupportState.MessageSent -> {
                    Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
                    loadMessages()
                }
                is com.esim.travelapp.presentation.viewmodel.SupportState.Error -> {
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun loadTickets() {
        supportViewModel.getUserTickets(currentUserId).observe(this) { tickets: List<SupportTicketEntity> ->
            Toast.makeText(this, "Loaded ${tickets.size} tickets", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMessages() {
        if (selectedTicketId > 0) {
            supportViewModel.getTicketMessages(selectedTicketId).observe(this) { messages: List<SupportMessageEntity> ->
                val adapter = SupportMessageAdapter(messages)
                messagesRecyclerView.adapter = adapter
            }
        }
    }
}
