package com.eventbox.app.android.search

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import io.reactivex.disposables.CompositeDisposable
import com.eventbox.app.android.event.Event
import com.eventbox.app.android.event.EventService

data class SearchEventsDataSourceFactory(
    private val compositeDisposable: CompositeDisposable,
    private val eventService: EventService,
    private val query: String,
    private val sortBy: String,
    private val mutableProgress: MutableLiveData<Boolean>
) : DataSource.Factory<Int, Event>() {
    override fun create(): DataSource<Int, Event> {
        return SearchEventsDataSource(
            eventService,
            compositeDisposable,
            query,
            sortBy,
            mutableProgress
        )
    }
}
