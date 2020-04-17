package com.eventbox.app.android.event.faq

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import com.eventbox.app.android.R
import com.eventbox.app.android.common.SingleLiveEvent
import com.eventbox.app.android.data.Resource
import com.eventbox.app.android.event.EventService
import com.eventbox.app.android.event.faq.EventFAQ
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class EventFAQViewModel(private val eventService: EventService, private val resource: Resource) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mutableEventFAQ = MutableLiveData<List<EventFAQ>>()
    val eventFAQ: LiveData<List<EventFAQ>> = mutableEventFAQ
    private val mutableError = SingleLiveEvent<String>()
    val error: SingleLiveEvent<String> = mutableError

    fun loadEventFaq(id: Long) {
        if (id == -1L) {
            mutableError.value = Resource().getString(R.string.error_fetching_event_message)
            return
        }
        compositeDisposable += eventService.getEventFAQs(id)
            .withDefaultSchedulers()
            .subscribe({ faqList ->
                mutableEventFAQ.value = faqList
            }, {
                mutableError.value = resource.getString(R.string.error_fetching_event_message)
                Timber.e(it, "Error fetching event %d", id)
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
