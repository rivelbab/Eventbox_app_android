package com.eventbox.app.android.ui.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import com.eventbox.app.android.R
import com.eventbox.app.android.ui.auth.AuthHolder
import com.eventbox.app.android.ui.common.SingleLiveEvent
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.models.event.FavoriteEvent
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.models.event.EventId
import com.eventbox.app.android.service.EventService
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class FavoriteEventsViewModel(
    private val eventService: EventService,
    private val resource: Resource,
    private val authHolder: AuthHolder
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableMessage = SingleLiveEvent<String>()
    val message: SingleLiveEvent<String> = mutableMessage
    private val mutableEvents = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = mutableEvents

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun loadFavoriteEvents() {
        compositeDisposable +=
            eventService.getFavoriteEvents()
                .withDefaultSchedulers()
                .subscribe({
                    mutableEvents.value = it
                    mutableProgress.value = false
                }, {
                    Timber.e(it, "Error fetching favorite events")
                    mutableMessage.value = resource.getString(R.string.fetch_favorite_events_error_message)
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
