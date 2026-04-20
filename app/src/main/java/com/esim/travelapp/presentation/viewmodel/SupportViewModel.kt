package com.esim.travelapp.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esim.travelapp.data.local.entity.SupportMessageEntity
import com.esim.travelapp.data.local.entity.SupportTicketEntity
import com.esim.travelapp.data.repository.SupportRepository
import kotlinx.coroutines.launch

class SupportViewModel(private val supportRepository: SupportRepository) : ViewModel() {

    private val _ticketState = MutableLiveData<SupportState>()
    val ticketState: LiveData<SupportState> = _ticketState

    private val _messageState = MutableLiveData<SupportState>()
    val messageState: LiveData<SupportState> = _messageState

    fun createTicket(userId: Int, subject: String, message: String, priority: String = "normal") {
        _ticketState.value = SupportState.Loading
        viewModelScope.launch {
            val result = supportRepository.createTicket(userId, subject, message, priority)
            result.onSuccess { ticketId ->
                _ticketState.value = SupportState.TicketCreated(ticketId)
            }.onFailure { error ->
                _ticketState.value = SupportState.Error(error.message ?: "Failed to create ticket")
            }
        }
    }

    fun sendMessage(ticketId: Int, senderId: Int, message: String) {
        _messageState.value = SupportState.Loading
        viewModelScope.launch {
            val result = supportRepository.sendMessage(ticketId, senderId, message)
            result.onSuccess {
                _messageState.value = SupportState.MessageSent
            }.onFailure { error ->
                _messageState.value = SupportState.Error(error.message ?: "Failed to send message")
            }
        }
    }

    fun getUserTickets(userId: Int): LiveData<List<SupportTicketEntity>> = supportRepository.getUserTickets(userId)

    fun getTicket(ticketId: Int): LiveData<SupportTicketEntity?> = supportRepository.getTicket(ticketId)

    fun getTicketMessages(ticketId: Int): LiveData<List<SupportMessageEntity>> = supportRepository.getTicketMessages(ticketId)

    fun closeTicket(ticketId: Int) {
        viewModelScope.launch {
            supportRepository.closeTicket(ticketId)
        }
    }

    fun getOpenTicketsCount(userId: Int) = supportRepository.getOpenTicketsCount(userId)
}

sealed class SupportState {
    object Loading : SupportState()
    object MessageSent : SupportState()
    data class TicketCreated(val ticketId: Int) : SupportState()
    data class Error(val message: String) : SupportState()
}
