package com.eventbox.app.android.event

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
import com.eventbox.app.android.auth.AuthHolder
import com.eventbox.app.android.common.SingleLiveEvent
import com.eventbox.app.android.event.Event
import com.eventbox.app.android.event.EventId
import com.eventbox.app.android.event.EventService
import com.eventbox.app.android.connectivity.MutableConnectionLiveData
import com.eventbox.app.android.data.Preference
import com.eventbox.app.android.data.Resource
import com.eventbox.app.android.event.paging.EventsDataSourceFactory
import com.eventbox.app.android.favorite.FavoriteEvent
import com.eventbox.app.android.search.location.SAVED_LOCATION
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import timber.log.Timber

const val NEW_NOTIFICATIONS = "newNotifications"

class EventsViewModel(
    private val eventService: EventService,
    private val preference: Preference,
    private val resource: Resource,
    private val mutableConnectionLiveData: MutableConnectionLiveData,
    private val config: PagedList.Config,
    private val authHolder: AuthHolder
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val connection: LiveData<Boolean> = mutableConnectionLiveData
    private val mutableProgress = MediatorLiveData<Boolean>()
    val progress: MediatorLiveData<Boolean> = mutableProgress
    private val mutablePagedEvents = MutableLiveData<PagedList<Event>>()
    val pagedEvents: LiveData<PagedList<Event>> = mutablePagedEvents
    private val mutableMessage = SingleLiveEvent<String>()
    val message: SingleLiveEvent<String> = mutableMessage
    var lastSearch = ""
    private val mutableSavedLocation = MutableLiveData<String>()
    val savedLocation: LiveData<String> = mutableSavedLocation
    private lateinit var sourceFactory: EventsDataSourceFactory

    fun loadLocation() {
        mutableSavedLocation.value = preference.getString(SAVED_LOCATION)
            ?: resource.getString(R.string.enter_location)
    }

    fun loadLocationEvents() {
        val location = mutableSavedLocation.value
        if (location == null || location == resource.getString(R.string.enter_location) ||
            location == resource.getString(R.string.no_location)) {
            mutableProgress.value = false
            return
        }

        sourceFactory = EventsDataSourceFactory(
            compositeDisposable,
            eventService,
            mutableSavedLocation.value,
            mutableProgress
        )
        val eventPagedList = RxPagedListBuilder(sourceFactory, config)
            .setFetchScheduler(Schedulers.io())
            .buildObservable()
            .cache()

        compositeDisposable += eventPagedList
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                val currentPagedEvents = mutablePagedEvents.value
                if (currentPagedEvents == null) {
                    mutablePagedEvents.value = it
                } else {
                    currentPagedEvents.addAll(it)
                    mutablePagedEvents.value = currentPagedEvents
                }
            }, {
                Timber.e(it, "Error fetching events")
                mutableMessage.value = resource.getString(R.string.error_fetching_events_message)
            })
    }
    fun isConnected(): Boolean = mutableConnectionLiveData.value ?: false

    fun clearEvents() {
        mutablePagedEvents.value = null
    }

    fun clearLastSearch() {
        lastSearch = ""
    }

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun setFavorite(event: Event, favorite: Boolean) {
        if (favorite) {
            addFavorite(event)
        } else {
            removeFavorite(event)
        }
    }

    private fun addFavorite(event: Event) {
        val favoriteEvent = FavoriteEvent(authHolder.getId(), EventId(event.id))
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

        val favoriteEvent = FavoriteEvent(favoriteEventId, EventId(event.id))
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
