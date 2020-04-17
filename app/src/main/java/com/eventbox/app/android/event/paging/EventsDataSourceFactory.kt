package com.eventbox.app.android.event.paging

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import io.reactivex.disposables.CompositeDisposable
import com.eventbox.app.android.event.Event
import com.eventbox.app.android.event.EventService
import com.eventbox.app.android.event.paging.EventsDataSource

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
