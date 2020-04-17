package com.eventbox.app.android.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import com.eventbox.app.android.R
import com.eventbox.app.android.common.SingleLiveEvent
import com.eventbox.app.android.data.Resource
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import com.eventbox.app.android.auth.User
import timber.log.Timber

class ProfileViewModel(
    private val authService: AuthService,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser
    private val mutableMessage = SingleLiveEvent<String>()
    val message: SingleLiveEvent<String> = mutableMessage
    private val mutableUpdatedUser = MutableLiveData<User>()
    val updatedUser: LiveData<User> = mutableUpdatedUser
    private val mutableUpdatedPassword = MutableLiveData<String>()
    val updatedPassword: LiveData<String> = mutableUpdatedPassword
    private val mutableAccountDeleted = MutableLiveData<Boolean>()
    val accountDeleted: LiveData<Boolean> = mutableAccountDeleted

    fun isLoggedIn() = authService.isLoggedIn()

    fun logout() {
        compositeDisposable += authService.logout()
            .withDefaultSchedulers()
            .subscribe({
                Timber.d("Logged out!")
            }) {
                Timber.e(it, "Failure Logging out!")
            }
    }

    fun deleteProfile() {
        compositeDisposable += authService.deleteProfile()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableAccountDeleted.value = true
                mutableMessage.value = resource.getString(R.string.success_deleting_account_message)
                logout()
            }, {
                mutableAccountDeleted.value = false
                mutableMessage.value = resource.getString(R.string.error_deleting_account_message)
            })
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        compositeDisposable += authService.changePassword(oldPassword, newPassword)
            .withDefaultSchedulers()
            .subscribe({
                if (it.passwordChanged) {
                    mutableMessage.value = resource.getString(R.string.change_password_success_message)
                    mutableUpdatedPassword.value = newPassword
                }
            }, {
                if (it.message.toString() == "HTTP 400 BAD REQUEST")
                    mutableMessage.value = resource.getString(R.string.incorrect_old_password_message)
                else mutableMessage.value = resource.getString(R.string.change_password_fail_message)
            })
    }

    fun getProfile() {
        compositeDisposable += authService.getProfile()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({ user ->
                Timber.d("Response Success")
                this.mutableUser.value = user
            }) {
                Timber.e(it, "Failure")
                mutableMessage.value = resource.getString(R.string.failure)
            }
    }

    fun syncProfile() {
        compositeDisposable += authService.syncProfile()
            .withDefaultSchedulers()
            .subscribe({ user ->
                Timber.d("Response Success")
                this.mutableUpdatedUser.value = user
            }) {
                Timber.e(it, "Failure")
            }
    }

    fun resendVerificationEmail(email: String) {
        compositeDisposable += authService.resendVerificationEmail(email)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableMessage.value = it.message
            }) {
                Timber.e(it, "Failure")
                mutableMessage.value = resource.getString(R.string.failure)
            }
    }

    fun verifyProfile(token: String) {
        compositeDisposable += authService.verifyEmail(token)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableMessage.value = it.message
                syncProfile()
            }) {
                Timber.e(it, "Error in verifying email")
                mutableMessage.value = resource.getString(R.string.verification_error_message)
            }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
