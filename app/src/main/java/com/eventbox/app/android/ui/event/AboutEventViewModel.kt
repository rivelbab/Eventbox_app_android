package com.eventbox.app.android.ui.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import com.eventbox.app.android.R
import com.eventbox.app.android.ui.common.SingleLiveEvent
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.service.EventService
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class AboutEventViewModel(private val eventService: EventService, private val resource: Resource) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgressAboutEvent = MutableLiveData<Boolean>()
    val progressAboutEvent: LiveData<Boolean> = mutableProgressAboutEvent
    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent
    private val mutableError = SingleLiveEvent<String>()
    val error: SingleLiveEvent<String> = mutableError

    fun loadEvent(id: String) {
        if (id == "") {
            mutableError.value = resource.getString(R.string.error_fetching_event_message)
            return
        }
        compositeDisposable += eventService.getEvent(id)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgressAboutEvent.value = true
            }.doFinally {
                mutableProgressAboutEvent.value = false
            }.subscribe({ eventList ->
                mutableEvent.value = eventList
            }, {
                mutableError.value = resource.getString(R.string.error_fetching_event_message)
                Timber.e(it, "Error fetching event %d", id)
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
