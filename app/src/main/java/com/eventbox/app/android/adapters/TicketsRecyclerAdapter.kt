package com.eventbox.app.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eventbox.app.android.R
import com.eventbox.app.android.models.payment.DiscountCode
import com.eventbox.app.android.models.payment.Tax
import com.eventbox.app.android.models.payment.Ticket
import com.eventbox.app.android.ui.payment.TicketViewHolder

class TicketsRecyclerAdapter : RecyclerView.Adapter<TicketViewHolder>() {

    private val tickets = ArrayList<Ticket>()
    private var eventCurrency: String? = null
    private var eventTimeZone: String? = null
    private var discountCode: DiscountCode? = null
    private var tax: Tax? = null
    private var selectedListener: TicketSelectedListener? = null
    private lateinit var ticketAndQuantity: List<Triple<Int, Int, Float>>

    fun addAll(ticketList: List<Ticket>) {
        if (tickets.isNotEmpty())
            this.tickets.clear()
        this.tickets.addAll(ticketList)
    }

    fun setSelectListener(listener: TicketSelectedListener?) {
        selectedListener = listener
    }

    fun setCurrency(currencyCode: String?) {
        eventCurrency = currencyCode
    }

    fun setTimeZone(timeZone: String?) {
        eventTimeZone = timeZone
        notifyDataSetChanged()
    }

    fun applyDiscount(discountCode: DiscountCode) {
        this.discountCode = discountCode
    }

    fun applyTax(tax: Tax) {
        this.tax = tax
    }

    fun cancelDiscountCode() {
        this.discountCode = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        var currentDiscountCode: DiscountCode? = null
        discountCode?.tickets?.forEach {
            if (it.id.toInt() == ticket.id)
                currentDiscountCode = discountCode
        }
        var qty = 0
        var donation = 0F
        if (this::ticketAndQuantity.isInitialized) {
            val ticketIndex = ticketAndQuantity.map { it.first }.indexOf(ticket.id)
            if (ticketIndex != -1) {
                qty = ticketAndQuantity[ticketIndex].second
                donation = ticketAndQuantity[ticketIndex].third
            }
        }

        holder.bind(ticket, selectedListener, eventCurrency, eventTimeZone, qty, donation, currentDiscountCode, tax)
    }

    override fun getItemCount(): Int {
        return tickets.size
    }

    fun setTicketAndQty(ticketAndQty: List<Triple<Int, Int, Float>>) {
        ticketAndQuantity = ticketAndQty
    }
}

interface TicketSelectedListener {
    fun onSelected(ticketId: Int, quantity: Int, donation: Float = 0F)
}
