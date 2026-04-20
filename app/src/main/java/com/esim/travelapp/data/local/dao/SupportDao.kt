package com.esim.travelapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.esim.travelapp.data.local.entity.SupportTicketEntity
import com.esim.travelapp.data.local.entity.SupportMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupportTicketDao {
    @Insert
    suspend fun createTicket(ticket: SupportTicketEntity): Long

    @Update
    suspend fun updateTicket(ticket: SupportTicketEntity)

    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserTickets(userId: Int): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets WHERE id = :ticketId")
    fun getTicket(ticketId: Int): Flow<SupportTicketEntity?>

    @Query("SELECT * FROM support_tickets WHERE id = :ticketId")
    suspend fun getTicketSync(ticketId: Int): SupportTicketEntity?

    @Query("SELECT COUNT(*) FROM support_tickets WHERE userId = :userId AND status = 'open'")
    fun getOpenTicketsCount(userId: Int): Flow<Int>
}

@Dao
interface SupportMessageDao {
    @Insert
    suspend fun sendMessage(message: SupportMessageEntity)

    @Query("SELECT * FROM support_messages WHERE ticketId = :ticketId ORDER BY timestamp ASC")
    fun getTicketMessages(ticketId: Int): Flow<List<SupportMessageEntity>>

    @Query("SELECT * FROM support_messages WHERE ticketId = :ticketId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessage(ticketId: Int): SupportMessageEntity?
}
