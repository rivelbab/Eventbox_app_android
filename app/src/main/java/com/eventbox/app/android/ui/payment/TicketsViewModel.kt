package com.eventbox.app.android.ui.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import com.eventbox.app.android.R
import com.eventbox.app.android.ui.auth.AuthHolder
import com.eventbox.app.android.ui.common.SingleLiveEvent
import com.eventbox.app.android.networks.connectivity.MutableConnectionLiveData
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.models.payment.DiscountCode
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.service.EventService
import com.eventbox.app.android.models.payment.Tax
import com.eventbox.app.android.models.payment.Ticket
import com.eventbox.app.android.service.TaxService
import com.eventbox.app.android.fragments.payment.APPLY_DISCOUNT_CODE
import com.eventbox.app.android.service.TicketService
import com.eventbox.app.android.utils.errors.HttpErrors
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import retrofit2.HttpException
import timber.log.Timber

class TicketsViewModel(
    private val ticketService: TicketService,
    private val eventService: EventService,
    private val authHolder: AuthHolder,
    private val resource: Resource,
    private val taxService: TaxService,
    private val mutableConnectionLiveData: MutableConnectionLiveData
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    val tickets = MutableLiveData<List<Ticket>>()
    val connection: LiveData<Boolean> = mutableConnectionLiveData
    private val mutableError = SingleLiveEvent<String>()
    val error: SingleLiveEvent<String> = mutableError
    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent
    private val mutableDiscountCodes = MutableLiveData<DiscountCode>()
    val discountCode: LiveData<DiscountCode> = mutableDiscountCodes
    private val mutableTaxInfo = MutableLiveData<Tax>()
    val taxInfo: LiveData<Tax> = mutableTaxInfo
    var appliedDiscountCode: DiscountCode? = null
    var totalTaxAmount = 0f
    private val mutableTicketTableVisibility = MutableLiveData<Boolean>()
    val ticketTableVisibility: LiveData<Boolean> = mutableTicketTableVisibility
    val ticketIdAndQty = MutableLiveData<List<Triple<Int, Int, Float>>>()
    var discountCodeCurrentLayout =
        APPLY_DISCOUNT_CODE
    var totalAmount: Float = 0.0f

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun loadTickets(id: Long) {
        if (id == -1L) {
            mutableError.value = resource.getString(R.string.error_fetching_tickets_message)
            return
        }
        compositeDisposable += ticketService.getTickets(id)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({ ticketList ->
                mutableProgress.value = false
                mutableTicketTableVisibility.value = ticketList.isNotEmpty()
                tickets.value = ticketList
            }, {
                mutableProgress.value = false
                mutableError.value = resource.getString(R.string.error_fetching_tickets_message)
                Timber.e(it, "Error fetching tickets %d", id)
            })
    }

    fun loadEvent(id: Long) {
        if (id == -1L) {
            throw IllegalStateException("ID should never be -1")
        }
        compositeDisposable += eventService.getEvent(id)
            .withDefaultSchedulers()
            .subscribe({
                mutableEvent.value = it
            }, {
                Timber.e(it, "Error fetching event %d", id)
                mutableError.value = resource.getString(R.string.error_fetching_event_message)
            })
    }

    fun fetchDiscountCode(eventId: Long, code: String) {
        compositeDisposable += ticketService.getDiscountCode(eventId, code)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableDiscountCodes.value = it
            }, {
                Timber.e(it, "Error fetching discount codes %s", code)
                if ((it as HttpException).code() == 404)
                    mutableError.value = resource.getString(R.string.invalid_discount_code)
                else
                    mutableError.value = resource.getString(R.string.error_fetching_discount_code)
            })
    }

    fun getAmount(ticketIdAndQty: List<Triple<Int, Int, Float>>): Float {
        val ticketIds = ArrayList<Int>()
        val tax = taxInfo.value
        var taxRate = 0f
        totalTaxAmount = 0f
        if (tax != null && !tax.isTaxIncludedInPrice) {
            taxRate = tax.rate ?: 0f
        }
        ticketIdAndQty.forEach {
            ticketIds.add(it.first)
        }
        val donation = ticketIdAndQty.map { it.third * it.second }.sum()
        tickets.value?.filter { ticketIds.contains(it.id) }?.let { tickets ->
            var prices = 0F
            val code = appliedDiscountCode
            tickets.forEach { ticket ->
                var price = ticket.price
                totalTaxAmount += (ticket.price * taxRate / 100) * ticketIdAndQty[tickets.indexOf(ticket)].second
                if (code?.value != null) {
                    appliedDiscountCode?.tickets?.forEach { ticketId ->
                        if (ticket.id == ticketId.id.toInt()) {
                            price -= if (code.type == AMOUNT) code.value else price * (code.value / 100)
                        }
                    }
                }
                price.let { prices += price * ticketIdAndQty[tickets.indexOf(ticket)].second }
            }
            prices += totalTaxAmount
            return prices + donation
        }
        return -1F
    }

    fun getTaxDetails(eventId: Long) {
        compositeDisposable += taxService.getTax(eventId)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableTaxInfo.value = it
            }, {
                if (it is HttpException)
                    if (it.code() == HttpErrors.NOT_FOUND)
                        Timber.e(it, "No tax for this event")
                    else
                        Timber.e(it, "Error fetching tax details")
            })
    }

    fun isConnected(): Boolean = mutableConnectionLiveData.value ?: false

    fun totalTicketsEmpty(ticketIdAndQty: List<Triple<Int, Int, Float>>): Boolean {
        return ticketIdAndQty.sumBy { it.second } == 0
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
