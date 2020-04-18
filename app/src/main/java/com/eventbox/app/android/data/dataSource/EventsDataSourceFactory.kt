package com.eventbox.app.android.data.dataSource

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.eventbox.app.android.data.dataSource.EventsDataSource
import io.reactivex.disposables.CompositeDisposable
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.service.EventService

class EventsDataSourceFactory(
    private val compositeDisposable: CompositeDisposable,
    private val eventService: EventService,
    private val query: String?,
    private val mutableProgress: MutableLiveData<Boolean>
) : DataSource.Factory<Int, Event>() {
    override fun create(): DataSource<Int, Event> {
        return EventsDataSource(
            eventService,
            compositeDisposable,
            query,
            mutableProgress
        )
    }
}
