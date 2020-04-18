package com.eventbox.app.android.ui.speakers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import com.eventbox.app.android.R
import com.eventbox.app.android.ui.common.SingleLiveEvent
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.models.speakers.Speaker
import com.eventbox.app.android.service.SpeakerService
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class SpeakerViewModel(
    private val speakerService: SpeakerService,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableSpeaker = MutableLiveData<Speaker>()
    val speaker: LiveData<Speaker> = mutableSpeaker
    private val mutableError = SingleLiveEvent<String>()
    val error: SingleLiveEvent<String> = mutableError

    fun loadSpeaker(id: Long) {
        if (id.equals(-1)) {
            mutableError.value = resource.getString(R.string.error_fetching_event_message)
            return
        }
        compositeDisposable += speakerService.fetchSpeaker(id)
            .withDefaultSchedulers()
            .subscribe({
                mutableSpeaker.value = it
            }, {
                Timber.e(it, "Error fetching speaker for id %d", id)
                mutableError.value = resource.getString(R.string.error_fetching_event_message)
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
