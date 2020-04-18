package com.eventbox.app.android.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eventbox.app.android.R
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import com.eventbox.app.android.ui.auth.AuthHolder
import com.eventbox.app.android.service.AuthService
import com.eventbox.app.android.networks.payloads.auth.RequestPasswordReset
import com.eventbox.app.android.models.auth.PasswordReset
import com.eventbox.app.android.ui.common.SingleLiveEvent
import com.eventbox.app.android.config.Preference
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.ui.event.NEW_NOTIFICATIONS
import com.eventbox.app.android.service.NotificationService
import com.eventbox.app.android.service.SettingsService
import com.eventbox.app.android.utils.errors.HttpErrors
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import retrofit2.HttpException
import timber.log.Timber

class StartupViewModel(
    private val preference: Preference,
    private val resource: Resource,
    private val authHolder: AuthHolder,
    private val authService: AuthService,
    private val notificationService: NotificationService,
    private val settingsService: SettingsService
) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()
    val mutableNewNotifications = MutableLiveData<Boolean>()
    val newNotifications: LiveData<Boolean> = mutableNewNotifications
    private val mutableDialogProgress = MutableLiveData<Boolean>()
    val dialogProgress: LiveData<Boolean> = mutableDialogProgress
    private val mutableIsRefresh = MutableLiveData<Boolean>()
    val isRefresh: LiveData<Boolean> = mutableIsRefresh
    private val mutableResetPasswordEmail = MutableLiveData<String>()
    val resetPasswordEmail: LiveData<String> = mutableResetPasswordEmail
    private val mutableMessage = SingleLiveEvent<String>()
    val message: SingleLiveEvent<String> = mutableMessage

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun getId() = authHolder.getId()

    fun syncNotifications() {
        if (!isLoggedIn())
            return
        compositeDisposable += notificationService.syncNotifications(getId())
            .withDefaultSchedulers()
            .subscribe({ list ->
                list?.forEach {
                    if (!it.isRead) {
                        preference.putBoolean(NEW_NOTIFICATIONS, true)
                        mutableNewNotifications.value = true
                    }
                }
            }, {
                if (it is HttpException) {
                    if (authHolder.isLoggedIn() && it.code() == HttpErrors.UNAUTHORIZED) {
                        logoutAndRefresh()
                    }
                }
                Timber.e(it, "Error fetching notifications")
            })
    }

    private fun logoutAndRefresh() {
        compositeDisposable += authService.logout()
            .withDefaultSchedulers()
            .subscribe({
                mutableIsRefresh.value = true
            }, {
                Timber.e(it, "Error while logout")
                mutableMessage.value = resource.getString(R.string.error)
            })
    }

    fun checkAndReset(token: String, newPassword: String) {
        val resetRequest =
            RequestPasswordReset(
                PasswordReset(
                    token,
                    newPassword
                )
            )
        if (authHolder.isLoggedIn()) {
            compositeDisposable += authService.logout()
                .withDefaultSchedulers()
                .doOnSubscribe {
                    mutableDialogProgress.value = true
                }.subscribe {
                    resetPassword(resetRequest)
                }
        } else
            resetPassword(resetRequest)
    }

    private fun resetPassword(resetRequest: RequestPasswordReset) {
        compositeDisposable += authService.resetPassword(resetRequest)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableDialogProgress.value = true
            }.doFinally {
                mutableDialogProgress.value = false
            }.subscribe({
                Timber.e(it.toString())
                mutableMessage.value = resource.getString(R.string.reset_password_message)
                mutableResetPasswordEmail.value = it.email
            }, {
                Timber.e(it, "Failed to reset password")
            })
    }

    fun fetchSettings() {
        compositeDisposable += settingsService.fetchSettings()
            .withDefaultSchedulers()
            .subscribe({
                Timber.d("Settings fetched successfully")
            }, {
                Timber.e(it, "Error in fetching settings form API")
            })
    }
}
