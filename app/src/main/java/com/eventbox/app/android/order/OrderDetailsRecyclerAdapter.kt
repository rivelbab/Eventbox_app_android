package com.eventbox.app.android.order

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.ArrayList
import com.eventbox.app.android.attendees.Attendee
import com.eventbox.app.android.databinding.ItemCardOrderDetailsBinding
import com.eventbox.app.android.event.Event
import com.eventbox.app.android.order.OrderDetailsViewHolder

class OrderDetailsRecyclerAdapter : RecyclerView.Adapter<OrderDetailsViewHolder>() {

    private val attendees = ArrayList<Attendee>()
    private var event: Event? = null
    private var orderIdentifier: String? = null
    private var eventDetailsListener: EventDetailsListener? = null
    private var onQrImageClicked: QrImageClickListener? = null

    fun addAll(attendeeList: List<Attendee>) {
        if (attendees.isNotEmpty())
            this.attendees.clear()
        this.attendees.addAll(attendeeList)
        notifyDataSetChanged()
    }

    fun setEvent(event: Event?) {
        this.event = event
        notifyDataSetChanged()
    }

    fun setSeeEventListener(listener: EventDetailsListener?) {
        eventDetailsListener = listener
    }

    fun setQrImageClickListener(listener: QrImageClickListener?) {
        onQrImageClicked = listener
    }

    fun setOrderIdentifier(orderId: String?) {
        orderIdentifier = orderId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailsViewHolder {
        val binding = ItemCardOrderDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderDetailsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderDetailsViewHolder, position: Int) {
        val order = attendees[position]
        val count = attendees.size
        holder.bind(order, event, orderIdentifier, eventDetailsListener, onQrImageClicked, count, position)
    }

    override fun getItemCount(): Int {
        return attendees.size
    }

    interface EventDetailsListener {
        fun onClick(eventID: Long)
    }

    interface QrImageClickListener {
        fun onClick(qrImage: Bitmap)
    }
}
