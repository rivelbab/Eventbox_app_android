package com.eventbox.app.android.data.dataSource

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.eventbox.app.android.data.dataSource.SimilarEventsDataSource
import io.reactivex.disposables.CompositeDisposable
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.service.EventService

class SimilarEventsDataSourceFactory(
    private val compositeDisposable: CompositeDisposable,
    private val topicId: Long,
    private val location: String?,
    private val eventId: Long,
    private val mutableProgress: MutableLiveData<Boolean>,
    private val eventService: EventService
) : DataSource.Factory<Int, Event>() {
    override fun create(): DataSource<Int, Event> {
        return SimilarEventsDataSource(
            compositeDisposable, topicId, location, eventId, mutableProgress, eventService
        )
    }
}
