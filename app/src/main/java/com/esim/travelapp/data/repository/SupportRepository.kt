package com.esim.travelapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.esim.travelapp.data.local.dao.SupportTicketDao
import com.esim.travelapp.data.local.dao.SupportMessageDao
import com.esim.travelapp.data.local.entity.SupportTicketEntity
import com.esim.travelapp.data.local.entity.SupportMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupportRepository(
    private val ticketDao: SupportTicketDao,
    private val messageDao: SupportMessageDao
) {

    suspend fun createTicket(
        userId: Int,
        subject: String,
        message: String,
        priority: String = "normal"
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val ticket = SupportTicketEntity(
                userId = userId,
                subject = subject,
                message = message,
                priority = priority,
                status = "open",
                createdAt = System.currentTimeMillis()
            )
            val ticketId = ticketDao.createTicket(ticket).toInt()
            Result.success(ticketId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserTickets(userId: Int): LiveData<List<SupportTicketEntity>> = ticketDao.getUserTickets(userId).asLiveData()

    fun getTicket(ticketId: Int): LiveData<SupportTicketEntity?> = ticketDao.getTicket(ticketId).asLiveData()

    suspend fun sendMessage(
        ticketId: Int,
        senderId: Int,
        message: String,
        isUserMessage: Boolean = true
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val msg = SupportMessageEntity(
                ticketId = ticketId,
                senderId = senderId,
                message = message,
                isUserMessage = isUserMessage,
                timestamp = System.currentTimeMillis()
            )
            messageDao.sendMessage(msg)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTicketMessages(ticketId: Int): LiveData<List<SupportMessageEntity>> = messageDao.getTicketMessages(ticketId).asLiveData()

    suspend fun closeTicket(ticketId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val ticket = ticketDao.getTicketSync(ticketId)
            if (ticket != null) {
                ticketDao.updateTicket(
                    ticket.copy(
                        status = "closed",
                        updatedAt = System.currentTimeMillis()
                    )
                )
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ticket not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getOpenTicketsCount(userId: Int) = ticketDao.getOpenTicketsCount(userId)
}
