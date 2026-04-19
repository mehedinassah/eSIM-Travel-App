package com.esim.travelapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import com.esim.travelapp.R
import com.esim.travelapp.data.local.entity.ESIMPlanEntity

class PlanAdapter(private val onPlanClick: (ESIMPlanEntity) -> Unit) :
    ListAdapter<ESIMPlanEntity, PlanAdapter.PlanViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plan, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(getItem(position), onPlanClick)
    }

    class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val countryText: TextView = itemView.findViewById(R.id.countryText)
        private val planNameText: TextView = itemView.findViewById(R.id.planNameText)
        private val dataText: TextView = itemView.findViewById(R.id.dataText)
        private val validityText: TextView = itemView.findViewById(R.id.validityText)
        private val priceText: TextView = itemView.findViewById(R.id.priceText)
        private val selectButton: Button = itemView.findViewById(R.id.selectButton)

        fun bind(plan: ESIMPlanEntity, onPlanClick: (ESIMPlanEntity) -> Unit) {
            countryText.text = plan.country
            planNameText.text = plan.planName
            dataText.text = "Data: ${plan.dataAmount}"
            validityText.text = "Validity: ${plan.validityDays} days"
            priceText.text = "₹${plan.price}"

            selectButton.setOnClickListener {
                onPlanClick(plan)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ESIMPlanEntity>() {
        override fun areItemsTheSame(oldItem: ESIMPlanEntity, newItem: ESIMPlanEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ESIMPlanEntity, newItem: ESIMPlanEntity) =
            oldItem == newItem
    }
}

class NotificationAdapter :
    ListAdapter<com.esim.travelapp.data.local.entity.NotificationEntity, NotificationAdapter.NotificationViewHolder>(
        DiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(notification: com.esim.travelapp.data.local.entity.NotificationEntity) {
            titleText.text = notification.title
            messageText.text = notification.message
            timeText.text = formatTime(notification.createdAt)
        }

        private fun formatTime(timestamp: Long): String {
            val diff = System.currentTimeMillis() - timestamp
            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000} min ago"
                diff < 86400000 -> "${diff / 3600000}h ago"
                else -> "${diff / 86400000}d ago"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<com.esim.travelapp.data.local.entity.NotificationEntity>() {
        override fun areItemsTheSame(
            oldItem: com.esim.travelapp.data.local.entity.NotificationEntity,
            newItem: com.esim.travelapp.data.local.entity.NotificationEntity
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: com.esim.travelapp.data.local.entity.NotificationEntity,
            newItem: com.esim.travelapp.data.local.entity.NotificationEntity
        ) = oldItem == newItem
    }
}

class PurchaseHistoryAdapter :
    ListAdapter<com.esim.travelapp.data.local.entity.PurchaseEntity, PurchaseHistoryAdapter.PurchaseViewHolder>(
        DiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_purchase_history, parent, false)
        return PurchaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PurchaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val planNameText: TextView = itemView.findViewById(R.id.planNameText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val statusText: TextView = itemView.findViewById(R.id.statusText)
        private val priceText: TextView = itemView.findViewById(R.id.priceText)

        fun bind(purchase: com.esim.travelapp.data.local.entity.PurchaseEntity) {
            planNameText.text = "Purchase #${purchase.id}"
            dateText.text = formatDate(purchase.purchaseDate)
            statusText.text = "Status: ${purchase.status.uppercase()}"
            priceText.text = "Pending amount"
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<com.esim.travelapp.data.local.entity.PurchaseEntity>() {
        override fun areItemsTheSame(
            oldItem: com.esim.travelapp.data.local.entity.PurchaseEntity,
            newItem: com.esim.travelapp.data.local.entity.PurchaseEntity
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: com.esim.travelapp.data.local.entity.PurchaseEntity,
            newItem: com.esim.travelapp.data.local.entity.PurchaseEntity
        ) = oldItem == newItem
    }
}
