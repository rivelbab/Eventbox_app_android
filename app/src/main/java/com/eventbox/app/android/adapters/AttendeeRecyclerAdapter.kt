package com.eventbox.app.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eventbox.app.android.models.attendees.Attendee
import com.eventbox.app.android.models.attendees.CustomForm
import com.eventbox.app.android.ui.attendees.AttendeeViewHolder
import com.eventbox.app.android.databinding.ItemAttendeeBinding
import com.eventbox.app.android.models.payment.Ticket

class AttendeeRecyclerAdapter : RecyclerView.Adapter<AttendeeViewHolder>() {
    private val attendeeList = ArrayList<Attendee>()
    private val ticketList = ArrayList<Ticket>()
    private var qty = ArrayList<Int>()
    private val customForm = ArrayList<CustomForm>()
    private var eventId: Long = -1
    var attendeeChangeListener: AttendeeDetailChangeListener? = null
    private var firstAttendee: Attendee? = null

    fun setEventId(newId: Long) {
        eventId = newId
    }

    fun setQuantity(ticketQuantities: List<Int>) {
        if (qty.isNotEmpty())qty.clear()
        qty.addAll(ticketQuantities)
    }

    fun addAllTickets(tickets: List<Ticket>) {
        if (ticketList.isNotEmpty()) ticketList.clear()
        tickets.forEachIndexed { index, ticket ->
            repeat(qty[index]) {
                ticketList.add(ticket)
            }
        }
        notifyDataSetChanged()
    }

    fun addAllAttendees(attendees: List<Attendee>) {
        if (attendeeList.isNotEmpty()) attendeeList.clear()
        attendeeList.addAll(attendees)
        notifyDataSetChanged()
    }

    fun setCustomForm(customForm: List<CustomForm>) {
        if (customForm.isNotEmpty()) this.customForm.clear()
        this.customForm.addAll(customForm)
        notifyDataSetChanged()
    }

    fun setFirstAttendee(attendee: Attendee?) {
        firstAttendee = attendee
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendeeViewHolder {
        val binding = ItemAttendeeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttendeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendeeViewHolder, position: Int) {
        holder.apply {
            if (attendeeList.size == ticketList.size)
                bind(attendeeList[position], ticketList[position], customForm, position, eventId, firstAttendee)
            onAttendeeDetailChanged = attendeeChangeListener
        }
    }

    override fun getItemCount(): Int {
        return attendeeList.size
    }
}

interface AttendeeDetailChangeListener {
    fun onAttendeeDetailChanged(attendee: Attendee, position: Int)
}
