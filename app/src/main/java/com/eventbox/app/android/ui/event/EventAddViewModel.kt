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
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import com.eventbox.app.android.models.user.User
import com.eventbox.app.android.networks.payloads.utils.UploadImage
import timber.log.Timber

class EventAddViewModel(
    private val authService: AuthService,
    private val authHolder: AuthHolder,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser
    private val mutableMessage = SingleLiveEvent<String>()
    val message: SingleLiveEvent<String> = mutableMessage
    private var addedImageTemp = MutableLiveData<File>()
    var encodedImage: String? = null

    fun getId() = authHolder.getId()

    fun isLoggedIn() = authService.isLoggedIn()

    fun addEvent(user: User) {
        val addEventObservable: Single<User> =
            if (encodedImage.isNullOrEmpty())
                authService.updateUser(user)
            else
                authService.uploadImage(
                    UploadImage(
                        encodedImage
                    )
                ).flatMap {
                    authService.updateUser(user.copy(avatarUrl = it.url))
                }

        compositeDisposable += addEventObservable
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }
            .doFinally {
                mutableProgress.value = false
            }
            .subscribe({
                mutableMessage.value = resource.getString(R.string.user_update_success_message)
                Timber.d("User updated")
            }) {
                mutableMessage.value = resource.getString(R.string.user_update_error_message)
                Timber.e(it, "Error updating user!")
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
