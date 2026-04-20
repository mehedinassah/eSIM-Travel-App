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
import com.esim.travelapp.data.local.entity.PurchaseEntity

class ActivePlanAdapter(private val onDetailsClick: (PurchaseEntity) -> Unit) :
    ListAdapter<PurchaseEntity, ActivePlanAdapter.ActivePlanViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivePlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_plan, parent, false)
        return ActivePlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivePlanViewHolder, position: Int) {
        holder.bind(getItem(position), onDetailsClick)
    }

    class ActivePlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val planNameText: TextView = itemView.findViewById(R.id.planName)
        private val countryText: TextView = itemView.findViewById(R.id.planCountry)
        private val dataText: TextView = itemView.findViewById(R.id.planData)
        private val validityText: TextView = itemView.findViewById(R.id.planValidity)
        private val detailsButton: Button = itemView.findViewById(R.id.detailsButton)
        private val renewButton: Button = itemView.findViewById(R.id.renewButton)

        fun bind(purchase: PurchaseEntity, onDetailsClick: (PurchaseEntity) -> Unit) {
            // Display purchase ID as placeholder (in real app, would fetch plan details)
            planNameText.text = "Plan #${purchase.planId}"
            dataText.text = "Active"
            validityText.text = "Soon"
            countryText.text = "Status: ${purchase.status}"

            detailsButton.setOnClickListener {
                onDetailsClick(purchase)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PurchaseEntity>() {
        override fun areItemsTheSame(oldItem: PurchaseEntity, newItem: PurchaseEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PurchaseEntity, newItem: PurchaseEntity) =
            oldItem == newItem
    }
}
