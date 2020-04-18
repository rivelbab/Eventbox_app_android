package com.eventbox.app.android.data.dataSource

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import io.reactivex.disposables.CompositeDisposable
import com.eventbox.app.android.ui.common.SingleLiveEvent
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.models.payment.Order
import com.eventbox.app.android.ui.payment.OrderFilter
import com.eventbox.app.android.service.OrderService
import com.eventbox.app.android.service.EventService

class OrderDataSourceFactory(
    private val orderService: OrderService,
    private val eventService: EventService,
    private val compositeDisposable: CompositeDisposable,
    private val showExpired: Boolean,
    private val mutableProgress: MutableLiveData<Boolean>,
    private val mutableNumOfTicket: MutableLiveData<Int>,
    private val mutableMessage: SingleLiveEvent<String>,
    private val userId: Long,
    private val orderFilter: OrderFilter,
    private val fromDb: Boolean
) : DataSource.Factory<Int, Pair<Event, Order>>() {
    override fun create(): DataSource<Int, Pair<Event, Order>> {
        return OrderDataSource(
            orderService,
            eventService,
            compositeDisposable,
            userId,
            showExpired,
            mutableProgress,
            mutableNumOfTicket,
            mutableMessage,
            orderFilter,
            fromDb
        )
    }
}
