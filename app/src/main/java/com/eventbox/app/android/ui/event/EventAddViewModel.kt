package com.eventbox.app.android.ui.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import java.io.File
import com.eventbox.app.android.R
import com.eventbox.app.android.ui.auth.AuthHolder
import com.eventbox.app.android.service.AuthService
import com.eventbox.app.android.ui.common.SingleLiveEvent
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import com.eventbox.app.android.models.user.User
import com.eventbox.app.android.networks.payloads.utils.UploadImage
import com.eventbox.app.android.service.EventService
import timber.log.Timber

class EventAddViewModel(
    private val eventService: EventService,
    private val authService: AuthService,
    private val authHolder: AuthHolder,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress

    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser

    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent

    private val mutableMessage = SingleLiveEvent<String>()
    val message: SingleLiveEvent<String> = mutableMessage

    private var addedImageTemp = MutableLiveData<File>()
    var encodedImage: String? = null

    var avatarUpdated = false
    var eventAvatar: String? = null

    fun getId() = authHolder.getId()

    fun isLoggedIn() = authService.isLoggedIn()

    fun addEvent(event: Event) {

        compositeDisposable += eventService.createEvent(event)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }
            .doFinally {
                mutableProgress.value = false
            }
            .subscribe({
                mutableMessage.value = resource.getString(R.string.add_event_success_message)
                Timber.d("Event added")
            }) {
                mutableMessage.value = resource.getString(R.string.add_event_failed_message)
                Timber.e(it, "Error added event!")
            }
    }

    fun setAddedTempFile(file: File) {
        addedImageTemp.value = file
    }

    fun getAddedTempFile(): MutableLiveData<File> {
        return addedImageTemp
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
