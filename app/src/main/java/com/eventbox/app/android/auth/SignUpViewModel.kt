package com.eventbox.app.android.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eventbox.app.android.auth.AuthService
import com.eventbox.app.android.auth.SignUp
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import com.eventbox.app.android.R
import com.eventbox.app.android.common.SingleLiveEvent
import com.eventbox.app.android.data.Network
import com.eventbox.app.android.data.Resource
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import com.eventbox.app.android.utils.nullToEmpty
import timber.log.Timber

class SignUpViewModel(
    private val authService: AuthService,
    private val network: Network,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableError = SingleLiveEvent<String>()
    val error: SingleLiveEvent<String> = mutableError
    private val mutableShowNoInternetDialog = MutableLiveData<Boolean>()
    val showNoInternetDialog: LiveData<Boolean> = mutableShowNoInternetDialog
    private val mutableLoggedIn = SingleLiveEvent<Boolean>()
    var loggedIn: SingleLiveEvent<Boolean> = mutableLoggedIn

    var email: String? = null
    var password: String? = null

    fun signUp(signUp: SignUp) {
        if (!isConnected()) return

        compositeDisposable += authService.signUp(signUp)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                login(signUp)
                Timber.d("Success!")
            }, {
                when {
                    it.toString().contains("HTTP 409") ->
                        mutableError.value = resource.getString(R.string.sign_up_fail_email_exist_message)
                    it.toString().contains("HTTP 422") ->
                        mutableError.value = resource.getString(R.string.sign_up_fail_email_invalid_message)
                    else -> mutableError.value = resource.getString(R.string.sign_up_fail_message)
                }
                Timber.d(it, "Failed")
            })
    }

    private fun login(signUp: SignUp) {
        if (!isConnected()) return
        email = signUp.email
        password = signUp.password
        compositeDisposable += authService.login(email.nullToEmpty(), password.nullToEmpty())
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableLoggedIn.value = true
                Timber.d("Success!")
                fetchProfile()
            }, {
                mutableError.value = resource.getString(R.string.login_automatically_fail_message)
                Timber.d(it, "Failed")
            })
    }

    private fun fetchProfile() {
        if (!isConnected()) return
        compositeDisposable += authService.getProfile()
            .withDefaultSchedulers()
            .subscribe({
                Timber.d("Fetched User Details")
            }) {
                Timber.e(it, "Error loading user details")
            }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    private fun isConnected(): Boolean {
        val isConnected = network.isNetworkConnected()
        if (!isConnected) mutableShowNoInternetDialog.value = true
        return isConnected
    }
}
