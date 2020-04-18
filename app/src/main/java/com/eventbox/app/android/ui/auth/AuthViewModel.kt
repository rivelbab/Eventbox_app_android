package com.eventbox.app.android.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eventbox.app.android.service.AuthService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import com.eventbox.app.android.R
import com.eventbox.app.android.ui.common.SingleLiveEvent
import com.eventbox.app.android.config.Network
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class AuthViewModel(
    private val authService: AuthService,
    private val network: Network,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    val mutableStatus = SingleLiveEvent<Boolean>()
    val isUserExists: LiveData<Boolean> = mutableStatus
    private val mutableError = SingleLiveEvent<String>()
    val error: SingleLiveEvent<String> = mutableError

    fun checkUser(email: String) {
        if (!network.isNetworkConnected()) {
            mutableError.value = resource.getString(R.string.no_internet_message)
            return
        }
        compositeDisposable += authService.checkEmail(email)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableStatus.value = !it.result
                Timber.d("Success!")
            }, {
                mutableError.value = resource.getString(R.string.error)
                Timber.d(it, "Failed")
            })
    }
}
