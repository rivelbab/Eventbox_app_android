package com.eventbox.app.android.ui.attendees

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import com.eventbox.app.android.R
import com.eventbox.app.android.models.attendees.Attendee
import com.eventbox.app.android.service.AttendeeService
import com.eventbox.app.android.models.attendees.CustomForm
import com.eventbox.app.android.ui.auth.AuthHolder
import com.eventbox.app.android.service.AuthService
import com.eventbox.app.android.models.user.User
import com.eventbox.app.android.ui.common.SingleLiveEvent
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.models.event.EventId
import com.eventbox.app.android.service.EventService
import com.eventbox.app.android.models.payment.Charge
import com.eventbox.app.android.models.payment.ConfirmOrder
import com.eventbox.app.android.models.payment.Order
import com.eventbox.app.android.service.OrderService
import com.eventbox.app.android.service.SettingsService
import com.eventbox.app.android.models.payment.Ticket
import com.eventbox.app.android.service.TicketService
import com.eventbox.app.android.utils.errors.ErrorUtils
import com.eventbox.app.android.utils.errors.HttpErrors
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import retrofit2.HttpException
import timber.log.Timber

const val ORDER_STATUS_PENDING = "pending"
const val ORDER_STATUS_COMPLETED = "completed"
const val ORDER_STATUS_PLACED = "placed"
const val ORDER_STATUS_CANCELLED = "cancelled"
const val ORDER_STATUS_INITIALIZING = "initializing"
const val PAYMENT_MODE_FREE = "free"
const val PAYMENT_MODE_BANK = "bank"
const val PAYMENT_MODE_ONSITE = "onsite"
const val PAYMENT_MODE_CHEQUE = "cheque"
const val PAYMENT_MODE_PAYPAL = "paypal"
const val PAYMENT_MODE_STRIPE = "stripe"
private const val ERRORS = "errors"
private const val DETAIL = "detail"
private const val UNVERIFIED_USER = "unverified-user"
private const val ORDER_EXPIRY_TIME = 15

class AttendeeViewModel(
    private val attendeeService: AttendeeService,
    private val authHolder: AuthHolder,
    private val eventService: EventService,
    private val orderService: OrderService,
    private val ticketService: TicketService,
    private val authService: AuthService,
    private val settingsService: SettingsService,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableTicketSoldOut = MutableLiveData<Boolean>()
    val ticketSoldOut: LiveData<Boolean> = mutableTicketSoldOut
    private val mutableMessage = SingleLiveEvent<String>()
    val message: SingleLiveEvent<String> = mutableMessage
    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent
    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser
    val orderCompleted = MutableLiveData<Boolean>()
    val totalAmount = MutableLiveData(0F)
    private val mutableTickets = MutableLiveData<List<Ticket>>()
    val tickets: LiveData<List<Ticket>> = mutableTickets
    private val mutableForms = MutableLiveData<List<CustomForm>>()
    val forms: LiveData<List<CustomForm>> = mutableForms
    private val mutablePendingOrder = MutableLiveData<Order>()
    val pendingOrder: LiveData<Order> = mutablePendingOrder
    private val mutableStripeOrderMade = MutableLiveData<Boolean>()
    val stripeOrderMade: LiveData<Boolean> = mutableStripeOrderMade
    private val mutableOrderExpiryTime = MutableLiveData<Int>()
    val orderExpiryTime: LiveData<Int> = mutableOrderExpiryTime
    private val mutableRedirectToProfile = SingleLiveEvent<Boolean>()
    val redirectToProfile = mutableRedirectToProfile
    private val mutablePaypalOrderMade = MutableLiveData<Boolean>()
    val paypalOrderMade: LiveData<Boolean> = mutablePaypalOrderMade

    val attendees = ArrayList<Attendee>()
    private val attendeesForOrder = ArrayList<Attendee>()
    private val ticketsForOrder = ArrayList<Ticket>()
    private var paymentModeForOrder: String =
        PAYMENT_MODE_FREE
    private var countryForOrder: String = ""
    private var companyForOrder: String = ""
    private var taxIdForOrder: String = ""
    private var addressForOrder: String = ""
    private var cityForOrder: String = ""
    private var postalCodeForOrder: String = ""
    private var stateForOrder: String = ""

    private var createAttendeeIterations = 0
    var orderIdentifier: String? = null
        private set
    private lateinit var confirmOrder: ConfirmOrder

    // Retained information
    var countryPosition: Int = -1
    var ticketIdAndQty: List<Triple<Int, Int, Float>>? = null
    var selectedPaymentMode: String = ""
    var singleTicket = false
    var monthSelectedPosition: Int = 0
    var yearSelectedPosition: Int = 0
    var paymentCurrency: String = ""
    var timeout: Long = -1L
    var ticketDetailsVisible = false
    var billingEnabled = false

    // Log in Information
    private val mutableSignedIn = MutableLiveData(true)
    val signedIn: LiveData<Boolean> = mutableSignedIn
    var isShowingSignInText = true

    fun getId() = authHolder.getId()

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun login(email: String, password: String) {

        compositeDisposable += authService.login(email, password)
            .withDefaultSchedulers()
            .subscribe({
                mutableSignedIn.value = true
            }, {
                mutableMessage.value = resource.getString(R.string.login_fail_message)
            })
    }

    fun logOut() {
        compositeDisposable += authService.logout()
            .withDefaultSchedulers()
            .subscribe({
                mutableSignedIn.value = false
                mutableUser.value = null
            }) {
                Timber.e(it, "Failure Logging out!")
            }
    }

    fun getTickets() {
        val ticketIds = ArrayList<Int>()
        ticketIdAndQty?.forEach {
            if (it.second > 0) {
                ticketIds.add(it.first)
            }
        }

        compositeDisposable += ticketService.getTicketsWithIds(ticketIds)
            .withDefaultSchedulers()
            .subscribe({ tickets ->
                mutableTickets.value = tickets
            }, {
                Timber.e(it, "Error Loading tickets!")
            })
    }

    fun initializeOrder(eventId: Long) {
        val emptyOrder = Order(
            id = getId(), status = ORDER_STATUS_INITIALIZING, event = EventId(
                eventId
            )
        )

        compositeDisposable += orderService.placeOrder(emptyOrder)
            .withDefaultSchedulers()
            .subscribe({
                mutablePendingOrder.value = it
                orderIdentifier = it.identifier.toString()
            }, {
                if (it is HttpException) {
                    if (ErrorUtils.getErrorDetails(it).code == UNVERIFIED_USER) {
                        mutableRedirectToProfile.value = true
                    }
                }
                Timber.e(it, "Fail on creating pending order")
            })
    }

    private fun createAttendee(attendee: Attendee, totalAttendee: Int) {
        compositeDisposable += attendeeService.postAttendee(attendee)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                attendeesForOrder.add(it)
                if (attendeesForOrder.size == totalAttendee) {
                    loadTicketsAndCreateOrder()
                    mutableMessage.value = resource.getString(R.string.create_attendee_success_message)
                }
                Timber.d("Success! %s", attendeesForOrder.toList().toString())
            }, {
                mutableProgress.value = false
                if (createAttendeeIterations + 1 == totalAttendee)
                    if (it is HttpException) {
                        if (it.code() == HttpErrors.CONFLICT)
                            mutableTicketSoldOut.value = true
                    } else {
                        mutableMessage.value = resource.getString(R.string.create_attendee_fail_message)
                        Timber.d(it, "Failed")
                        mutableTicketSoldOut.value = false
                    }
            })
    }

    fun createAttendees(
        attendees: List<Attendee>,
        country: String?,
        company: String,
        taxId: String,
        address: String,
        city: String,
        postalCode: String,
        state: String,
        paymentMode: String
    ) {
        attendeesForOrder.clear()
        countryForOrder = country ?: ""
        companyForOrder = company
        taxIdForOrder = taxId
        addressForOrder = address
        cityForOrder = city
        postalCodeForOrder = postalCode
        stateForOrder = state
        paymentModeForOrder = paymentMode
        var isAllDetailsFilled = true
        createAttendeeIterations = 0
        attendees.forEach {
            if (it.email.isNullOrBlank() || it.firstname.isNullOrBlank() || it.lastname.isNullOrBlank()) {
                if (isAllDetailsFilled)
                    mutableMessage.value = resource.getString(R.string.fill_all_fields_message)
                isAllDetailsFilled = false
                return
            }
        }
        if (isAllDetailsFilled) {
            attendees.forEach {
                createAttendee(it, attendees.size)
            }
        }
    }

    private fun loadTicketsAndCreateOrder() {
        ticketsForOrder.clear()
        attendeesForOrder.forEach {
            loadTicket(it.ticket?.id)
        }
    }

    private fun loadTicket(ticketId: Long?) {
        if (ticketId == null) {
            Timber.e("TicketId cannot be null")
            return
        }
        compositeDisposable += ticketService.getTicketDetails(ticketId)
            .withDefaultSchedulers()
            .subscribe({
                ticketsForOrder.add(it)
                Timber.d("Loaded tickets! %s", ticketsForOrder.toList().toString())
                if (ticketsForOrder.size == attendeesForOrder.size) {
                    createOrder()
                }
            }, {
                Timber.d(it, "Error loading Ticket!")
            })
    }

    private fun createOrder() {
        var order = mutablePendingOrder.value
        val identifier: String? = orderIdentifier
        if (order == null || identifier == null) {
            mutableMessage.value = resource.getString(R.string.order_fail_message)
            return
        }
        val amount: Float = totalAmount.value ?: 0F
        if (amount <= 0) {
            paymentModeForOrder =
                PAYMENT_MODE_FREE
        }
        order = order.copy(attendees = attendeesForOrder, paymentMode = paymentModeForOrder, amount = amount)
        if (billingEnabled) {
            order = order.copy(isBillingEnabled = true, company = companyForOrder, taxBusinessInfo = taxIdForOrder,
                address = addressForOrder, city = cityForOrder, zipcode = postalCodeForOrder, country = countryForOrder,
                state = stateForOrder)
        }
        compositeDisposable += orderService.placeOrder(order)
            .withDefaultSchedulers()
            .subscribe({
                orderIdentifier = it.identifier.toString()
                Timber.d("Success placing order!")
                when (it.paymentMode) {
                    PAYMENT_MODE_FREE -> {
                        confirmOrder =
                            ConfirmOrder(
                                it.id.toString(),
                                ORDER_STATUS_COMPLETED
                            )
                        confirmOrderStatus(it.identifier.toString(), confirmOrder)
                    }
                    PAYMENT_MODE_CHEQUE, PAYMENT_MODE_BANK, PAYMENT_MODE_ONSITE -> {
                        confirmOrder =
                            ConfirmOrder(
                                it.id.toString(),
                                ORDER_STATUS_PLACED
                            )
                        confirmOrderStatus(it.identifier.toString(), confirmOrder)
                    }
                    PAYMENT_MODE_STRIPE -> {
                        mutableStripeOrderMade.value = true
                    }
                    PAYMENT_MODE_PAYPAL -> {
                        mutablePendingOrder.value = it
                        mutablePaypalOrderMade.value = true
                        mutableProgress.value = false
                    }
                    else -> mutableMessage.value = resource.getString(R.string.order_success_message)
                }
            }, {
                mutableMessage.value = resource.getString(R.string.order_fail_message)
                Timber.d(it, "Failed creating Order")
                mutableProgress.value = false
                deleteAttendees(order.attendees)
            })
    }

    fun sendPaypalConfirm(paymentId: String) {
        pendingOrder.value?.let { order ->
            compositeDisposable += orderService.verifyPaypalPayment(order.identifier.toString(), paymentId)
                .withDefaultSchedulers()
                .doOnSubscribe {
                    mutableProgress.value = true
                }.subscribe({
                    if (it.status) {
                        confirmOrder =
                            ConfirmOrder(
                                order.id.toString(),
                                ORDER_STATUS_COMPLETED
                            )
                        confirmOrderStatus(order.identifier.toString(), confirmOrder)
                    } else {
                        mutableMessage.value = it.error
                        mutableProgress.value = false
                    }
                }, {
                    Timber.e(it, "Error verifying paypal payment")
                    mutableMessage.value = resource.getString(R.string.error_making_paypal_payment_message)
                    mutableProgress.value = false
                })
        }
    }

    private fun confirmOrderStatus(identifier: String, order: ConfirmOrder) {
        compositeDisposable += orderService.confirmOrder(identifier, order)
            .withDefaultSchedulers()
            .doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableMessage.value = resource.getString(R.string.order_success_message)
                Timber.d("Updated order status successfully !")
                orderCompleted.value = true
            }, {
                mutableMessage.value = resource.getString(R.string.order_fail_message)
                Timber.d(it, "Failed updating order status")
            })
    }

    fun getCustomFormsForAttendees(eventId: Long) {
        compositeDisposable += attendeeService.getCustomFormsForAttendees(eventId)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                mutableProgress.value = false
                mutableForms.value = it
                Timber.d("Forms fetched successfully !")
            }, {
                Timber.d(it, "Failed fetching forms")
            })
    }

    private fun deleteAttendees(attendees: List<Attendee>?) {
        attendees?.forEach { attendee ->
            compositeDisposable += attendeeService.deleteAttendee(attendee.id)
                .withDefaultSchedulers()
                .subscribe({
                    Timber.d("Deleted attendee ${attendee.id}")
                }, {
                    Timber.d("Failed to delete attendee $it.id")
                })
        }
    }

    fun chargeOrder(charge: Charge) {
        compositeDisposable += orderService.chargeOrder(orderIdentifier.toString(), charge)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableMessage.value = it.message
                if (it.status != null && it.status) {
                    confirmOrder =
                        ConfirmOrder(
                            it.id.toString(),
                            ORDER_STATUS_COMPLETED
                        )
                    confirmOrderStatus(orderIdentifier.toString(), confirmOrder)
                    Timber.d("Successfully  charged for the order!")
                } else {
                    Timber.d("Failed charging the user")
                }
            }, {
                mutableMessage.value = ErrorUtils.getErrorDetails(it).detail
                Timber.d(it, "Failed charging the user")
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
                mutableMessage.value = resource.getString(R.string.error_fetching_event_message)
            })
    }

    fun loadUser() {
        val id = getId()
        if (id == -1L) {
            throw IllegalStateException("ID should never be -1")
        }
        compositeDisposable += attendeeService.getAttendeeDetails(id)
            .withDefaultSchedulers()
            .subscribe({
                mutableUser.value = it
            }, {
                Timber.e(it, "Error fetching user %d", id)
            })
    }

    fun areAttendeeEmailsValid(attendees: ArrayList<Attendee>): Boolean {
        /**Checks for  correct pattern in email*/
        attendees.forEach {
            if (it.email.isNullOrEmpty()) return false
            else if (!Patterns.EMAIL_ADDRESS.matcher(it.email).matches()) return false
        }
        return true
    }

    fun getSettings() {
        compositeDisposable += settingsService.fetchSettings()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableOrderExpiryTime.value = it.orderExpiryTime
            }, {
                mutableOrderExpiryTime.value =
                    ORDER_EXPIRY_TIME
                Timber.e(it, "Error fetching settings")
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
