package com.esim.travelapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.esim.travelapp.R
import com.esim.travelapp.data.local.entity.SupportMessageEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SupportMessageAdapter(private val messages: List<SupportMessageEntity>) : RecyclerView.Adapter<SupportMessageAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val senderText: TextView = itemView.findViewById(R.id.senderText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(message: SupportMessageEntity) {
            messageText.text = message.message
            senderText.text = if (message.isUserMessage) "You" else "Support"
            val sdf = SimpleDateFormat("MMM dd, hh:mm aa", Locale.getDefault())
            timeText.text = sdf.format(Date(message.timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_support_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size
}
