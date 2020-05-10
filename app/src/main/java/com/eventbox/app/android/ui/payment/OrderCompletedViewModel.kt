package com.eventbox.app.android.ui.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import com.eventbox.app.android.R
import com.eventbox.app.android.ui.auth.AuthHolder
import com.eventbox.app.android.ui.common.SingleLiveEvent
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.models.event.EventId
import com.eventbox.app.android.service.EventService
import com.eventbox.app.android.data.dataSource.SimilarEventsDataSourceFactory
import com.eventbox.app.android.models.event.FavoriteEvent
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class OrderCompletedViewModel(
    private val eventService: EventService,
    private val resource: Resource,
    private val config: PagedList.Config,
    private val authHolder: AuthHolder
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableMessage = SingleLiveEvent<String>()
    val message: SingleLiveEvent<String> = mutableMessage
    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableSimilarEvents = MediatorLiveData<PagedList<Event>>()
    val similarEvents: MediatorLiveData<PagedList<Event>> = mutableSimilarEvents

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun loadEvent(id: Long) {
        if (id == -1L) {
            throw IllegalStateException("ID should never be -1")
        }

        compositeDisposable +=
            eventService.getEvent(id)
                .withDefaultSchedulers()
                .subscribe({
                    mutableEvent.value = it
                }, {
                    Timber.e(it, "Error fetching event %d", id)
                    mutableMessage.value = resource.getString(R.string.error_fetching_event_message)
                })
    }

    fun fetchSimilarEvents(eventId: Long, topicId: Long, location: String?) {
        if (eventId == -1L) return

        val sourceFactory =
            SimilarEventsDataSourceFactory(
                compositeDisposable,
                topicId,
                eventId,
                mutableProgress,
                eventService
            )

        val similarEventPagedList = RxPagedListBuilder(sourceFactory, config)
            .setFetchScheduler(Schedulers.io())
            .buildObservable()
            .cache()

        compositeDisposable += similarEventPagedList
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({ events ->
                val currentPagedSimilarEvents = mutableSimilarEvents.value
                if (currentPagedSimilarEvents == null) {
                    mutableSimilarEvents.value = events
                } else {
                    currentPagedSimilarEvents.addAll(events)
                    mutableSimilarEvents.value = currentPagedSimilarEvents
                }
            }, {
                Timber.e(it, "Error fetching similar events")
            })
    }

    fun setFavorite(event: Event, favorite: Boolean) {
        if (favorite) {
            addFavorite(event)
        } else {
            removeFavorite(event)
        }
    }

    private fun addFavorite(event: Event) {
        val favoriteEvent = FavoriteEvent(
            authHolder.getId(),
            EventId(event.id)
        )
        compositeDisposable += eventService.addFavorite(favoriteEvent, event)
            .withDefaultSchedulers()
            .subscribe({
                mutableMessage.value = resource.getString(R.string.add_event_to_shortlist_message)
            }, {
                mutableMessage.value = resource.getString(R.string.out_bad_try_again)
                Timber.d(it, "Fail on adding like for event ID ${event.id}")
            })
    }

    private fun removeFavorite(event: Event) {
        val favoriteEventId = event.favoriteEventId ?: return

        val favoriteEvent = FavoriteEvent(
            favoriteEventId,
            EventId(event.id)
        )
        compositeDisposable += eventService.removeFavorite(favoriteEvent, event)
            .withDefaultSchedulers()
            .subscribe({
                mutableMessage.value = resource.getString(R.string.remove_event_from_shortlist_message)
            }, {
                mutableMessage.value = resource.getString(R.string.out_bad_try_again)
                Timber.d(it, "Fail on removing like for event ID ${event.id}")
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
