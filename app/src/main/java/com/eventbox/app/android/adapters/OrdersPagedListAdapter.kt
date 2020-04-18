package com.eventbox.app.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.eventbox.app.android.databinding.ItemCardOrderBinding
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.models.payment.Order
import com.eventbox.app.android.ui.payment.OrdersViewHolder

class OrdersPagedListAdapter : PagedListAdapter<Pair<Event, Order>, OrdersViewHolder>(
    OrdersDiffCallback()
) {

    private var showExpired = false
    private var clickListener: OrderClickListener? = null

    fun setListener(listener: OrderClickListener?) {
        clickListener = listener
    }

    fun setShowExpired(expired: Boolean) {
        showExpired = expired
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val binding = ItemCardOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrdersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item, showExpired, clickListener)
        }
    }

    fun clear() {
        this.submitList(null)
    }

    interface OrderClickListener {
        fun onClick(eventID: Long, orderIdentifier: String, orderId: Long)
    }
}

class OrdersDiffCallback : DiffUtil.ItemCallback<Pair<Event, Order>>() {
    override fun areItemsTheSame(oldItem: Pair<Event, Order>, newItem: Pair<Event, Order>): Boolean {
        return oldItem.second.id == newItem.second.id
    }

    override fun areContentsTheSame(oldItem: Pair<Event, Order>, newItem: Pair<Event, Order>): Boolean {
        return oldItem == newItem
    }
}
