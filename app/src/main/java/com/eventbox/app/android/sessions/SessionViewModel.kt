package com.eventbox.app.android.sessions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import com.eventbox.app.android.BuildConfig.MAPBOX_KEY
import com.eventbox.app.android.R
import com.eventbox.app.android.common.SingleLiveEvent
import com.eventbox.app.android.data.Resource
import com.eventbox.app.android.speakers.Speaker
import com.eventbox.app.android.speakers.SpeakerService
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class SessionViewModel(
    private val sessionService: SessionService,
    private val speakerService: SpeakerService,
    private val resource: Resource
) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private val mutableSession = MutableLiveData<Session>()
    val session: LiveData<Session> = mutableSession
    private val mutableProgress = MutableLiveData<Boolean>(true)
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableError = SingleLiveEvent<String>()
    val error: SingleLiveEvent<String> = mutableError
    private val mutableSpeakers = MutableLiveData<List<Speaker>>()
    val speakersUnderSession: LiveData<List<Speaker>> = mutableSpeakers

    fun loadSession(id: Long) {
        if (id == -1L) {
            mutableError.value = resource.getString(R.string.error_fetching_event_message)
            return
        }

        compositeDisposable += sessionService.fetchSession(id)
            .withDefaultSchedulers()
            .doOnSubscribe { mutableProgress.value = true }
            .doFinally { mutableProgress.value = false }
            .subscribe({
                mutableSession.value = it
            }, {
                Timber.e(it, "Error fetching session id $id")
                mutableError.value = resource.getString(R.string.error_fetching_event_section_message,
                    resource.getString(R.string.session))
            })
    }

    fun loadSpeakersUnderSession(id: Long) {
        if (id == -1L) {
            mutableError.value = resource.getString(R.string.error_fetching_speakers_for_session)
            return
        }

        compositeDisposable += speakerService.fetchSpeakerForSession(id)
            .withDefaultSchedulers()
            .subscribe({
                mutableSpeakers.value = it
            }, {
                Timber.e(it, "Error fetching speakers for session $id")
                mutableError.value = resource.getString(R.string.error_fetching_speakers_for_session)
            })
    }

    fun loadMap(latitude: String, longitude: String): String {
        // location handling
        val BASE_URL = "https://api.mapbox.com/v4/mapbox.emerald/pin-l-marker+673ab7"
        val LOCATION = "($longitude,$latitude)/$longitude,$latitude"
        return "$BASE_URL$LOCATION,15/900x500.png?access_token=$MAPBOX_KEY"
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
