package com.esim.travelapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import com.esim.travelapp.R
import com.esim.travelapp.data.local.entity.PurchaseEntity
import com.esim.travelapp.data.local.entity.ESIMPlanEntity
import com.esim.travelapp.data.local.entity.DataUsageEntity
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ActivePlanDisplayModel(
    val purchase: PurchaseEntity,
    val plan: ESIMPlanEntity,
    val dataUsage: DataUsageEntity?
)

class ActivePlanAdapter(
    private val onDetailsClick: (PurchaseEntity) -> Unit,
    private val onRenewClick: (PurchaseEntity) -> Unit = {}
) :
    ListAdapter<ActivePlanDisplayModel, ActivePlanAdapter.ActivePlanViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivePlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_plan, parent, false)
        return ActivePlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivePlanViewHolder, position: Int) {
        holder.bind(getItem(position), onDetailsClick, onRenewClick)
    }

    class ActivePlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val planNameText: TextView = itemView.findViewById(R.id.planName)
        private val countryText: TextView = itemView.findViewById(R.id.planCountry)
        private val dataText: TextView = itemView.findViewById(R.id.planData)
        private val validityText: TextView = itemView.findViewById(R.id.planValidity)
        private val detailsButton: Button = itemView.findViewById(R.id.detailsButton)
        private val renewButton: Button = itemView.findViewById(R.id.renewButton)
        private val dataUsageProgressBar: ProgressBar? = itemView.findViewById(R.id.dataProgressBar)
        private val dataUsageText: TextView? = itemView.findViewById(R.id.dataUsageText)

        fun bind(
            model: ActivePlanDisplayModel,
            onDetailsClick: (PurchaseEntity) -> Unit,
            onRenewClick: (PurchaseEntity) -> Unit
        ) {
            val purchase = model.purchase
            val plan = model.plan
            val usage = model.dataUsage

            // Display plan details
            planNameText.text = plan.planName
            countryText.text = plan.country
            dataText.text = plan.dataAmount

            // Calculate days remaining
            val purchaseDateMs = purchase.purchaseDate
            val expiryDateMs = purchaseDateMs + TimeUnit.DAYS.toMillis(plan.validityDays.toLong())
            val currentTimeMs = System.currentTimeMillis()
            val daysRemaining = TimeUnit.MILLISECONDS.toDays(expiryDateMs - currentTimeMs).toInt()

            validityText.text = when {
                daysRemaining > 1 -> "$daysRemaining days"
                daysRemaining == 1 -> "1 day"
                daysRemaining <= 0 -> "Expired"
                else -> "N/A"
            }

            // Display data usage if available
            if (usage != null) {
                dataUsageText?.text = "${String.format("%.1f", usage.dataUsed)}/${String.format("%.1f", usage.dataTotal)} GB"
                val usagePercent = if (usage.dataTotal > 0) {
                    ((usage.dataUsed / usage.dataTotal) * 100).toInt()
                } else {
                    0
                }
                dataUsageProgressBar?.progress = usagePercent.coerceIn(0, 100)
            } else {
                dataUsageText?.text = "0/${plan.dataAmount}"
                dataUsageProgressBar?.progress = 0
            }

            detailsButton.setOnClickListener {
                onDetailsClick(purchase)
            }

            renewButton.setOnClickListener {
                onRenewClick(purchase)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ActivePlanDisplayModel>() {
        override fun areItemsTheSame(oldItem: ActivePlanDisplayModel, newItem: ActivePlanDisplayModel) =
            oldItem.purchase.id == newItem.purchase.id

        override fun areContentsTheSame(oldItem: ActivePlanDisplayModel, newItem: ActivePlanDisplayModel) =
            oldItem == newItem
    }
}
