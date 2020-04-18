package com.eventbox.app.android.ui.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import com.eventbox.app.android.data.dataSource.OrderDataSourceFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import com.eventbox.app.android.ui.auth.AuthHolder
import com.eventbox.app.android.ui.common.SingleLiveEvent
import com.eventbox.app.android.networks.connectivity.MutableConnectionLiveData
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.models.payment.Order
import com.eventbox.app.android.service.EventService
import com.eventbox.app.android.service.OrderService
import timber.log.Timber

class OrdersUnderUserViewModel(
    private val orderService: OrderService,
    private val eventService: EventService,
    private val authHolder: AuthHolder,
    private val mutableConnectionLiveData: MutableConnectionLiveData,
    private val config: PagedList.Config
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val connection: LiveData<Boolean> = mutableConnectionLiveData
    private val mutableMessage = SingleLiveEvent<String>()
    val message: SingleLiveEvent<String> = mutableMessage
    private val mutableEventAndOrderPaged = MutableLiveData<PagedList<Pair<Event, Order>>>()
    val eventAndOrderPaged: LiveData<PagedList<Pair<Event, Order>>> = mutableEventAndOrderPaged
    private val mutableShowShimmerResults = MutableLiveData<Boolean>()
    val showShimmerResults: LiveData<Boolean> = mutableShowShimmerResults
    private val mutableNumOfTickets = MutableLiveData(0)
    val numOfTickets: LiveData<Int> = mutableNumOfTickets

    // Retain filter options
    val filter = OrderFilter()

    fun getId() = authHolder.getId()

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun getOrdersAndEventsOfUser(showExpired: Boolean, fromDb: Boolean) {

        val sourceFactory =
            OrderDataSourceFactory(
                orderService,
                eventService,
                compositeDisposable,
                showExpired,
                mutableShowShimmerResults,
                mutableNumOfTickets,
                mutableMessage,
                getId(),
                filter,
                fromDb
            )

        val ordersAndEventsPagedList = RxPagedListBuilder(sourceFactory, config)
            .setFetchScheduler(Schedulers.io())
            .buildObservable()
            .cache()

        compositeDisposable += ordersAndEventsPagedList
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .doOnSubscribe {
                mutableShowShimmerResults.value = true
            }.subscribe({
                val currentPagedOrdersAndEvents = mutableEventAndOrderPaged.value
                if (currentPagedOrdersAndEvents == null) {
                    mutableEventAndOrderPaged.value = it
                } else {
                    currentPagedOrdersAndEvents.addAll(it)
                    mutableEventAndOrderPaged.value = currentPagedOrdersAndEvents
                }
            }, {
                Timber.d(it, "Failed  to list events under a user ")
            })
    }

    fun clearOrders() {
        mutableEventAndOrderPaged.value = null
        mutableNumOfTickets.value = 0
    }

    fun isConnected(): Boolean = mutableConnectionLiveData.value ?: false

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}

class OrderFilter(
    var isShowingCompletedOrders: Boolean = true,
    var isShowingPendingOrders: Boolean = true,
    var isShowingPlacedOrders: Boolean = true,
    var isSortingOrdersByDate: Boolean = true
)
