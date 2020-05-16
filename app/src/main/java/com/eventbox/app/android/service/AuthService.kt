package com.eventbox.app.android.service

import com.eventbox.app.android.networks.api.AuthApi
import com.eventbox.app.android.ui.auth.AuthHolder
import io.reactivex.Completable
import io.reactivex.Single
import com.eventbox.app.android.networks.payloads.auth.ChangeRequestToken
import com.eventbox.app.android.networks.payloads.auth.ChangeRequestTokenResponse
import com.eventbox.app.android.models.auth.Password
import com.eventbox.app.android.models.auth.Email
import com.eventbox.app.android.networks.payloads.auth.RequestToken
import com.eventbox.app.android.networks.payloads.auth.RequestTokenResponse
import com.eventbox.app.android.data.dao.UserDao
import com.eventbox.app.android.data.dao.EventDao
import com.eventbox.app.android.models.auth.Login
import com.eventbox.app.android.models.auth.SignUp
import com.eventbox.app.android.models.auth.Token
import com.eventbox.app.android.models.user.User
import com.eventbox.app.android.networks.payloads.auth.*
import com.eventbox.app.android.networks.payloads.utils.ImageResponse
import com.eventbox.app.android.networks.payloads.utils.UploadImage
import timber.log.Timber

class AuthService(
    private val authApi: AuthApi,
    private val authHolder: AuthHolder,
    private val userDao: UserDao,
    private val eventDao: EventDao
) {
    fun login(username: String, password: String): Single<LoginResponse> {
        if (username.isEmpty() || password.isEmpty())
            throw IllegalArgumentException("Username or password cannot be empty")

        return authApi.login(
            Login(
                username,
                password
            )
        )
        .map {
            authHolder.token = it.accessToken
            it
        }
    }

    fun checkPasswordValid(email: String, password: String): Single<LoginResponse> =
        authApi.login(Login(email, password))

    fun signUp(signUp: SignUp): Single<User> {
        val email = signUp.email
        val password = signUp.password
        if (email.isNullOrEmpty() || password.isNullOrEmpty())
            throw IllegalArgumentException("Username or password cannot be empty")

        return authApi.signUp(signUp)
    }

    fun updateUser(user: User): Single<User> {
        return authApi.updateUser(user, user.id)
    }

    fun uploadImage(uploadImage: UploadImage): Single<ImageResponse> {
        return authApi.uploadImage(uploadImage)
    }

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun getId() = authHolder.getId()

    fun logout(): Completable {
        return Completable.fromAction {
            authHolder.token = null
            eventDao.clearFavoriteEvents()
        }
    }

    fun deleteProfile(userId: String = authHolder.getId()) =
        authApi.deleteAccount(userId)

    fun getProfile(id: String = authHolder.getId()): Single<User> {
        return authApi.getProfile(id)
    }

    fun syncProfile(id: String = authHolder.getId()): Single<User> {
        return authApi.getProfile(id)
    }

    fun sendResetPasswordEmail(email: String): Single<RequestTokenResponse> {
        val requestToken =
            RequestToken(
                Email(
                    email
                )
            )
        return authApi.requestToken(requestToken)
    }

    fun changePassword(oldPassword: String, newPassword: String): Single<ChangeRequestTokenResponse> {
        val changeRequestToken =
            ChangeRequestToken(
                Password(
                    oldPassword,
                    newPassword
                )
            )
        return authApi.changeRequestToken(changeRequestToken)
    }

    fun checkEmail(email: String): Single<CheckEmailResponse> {
        return authApi.checkEmail(
            Email(
                email
            )
        )
    }

    fun resendVerificationEmail(email: String): Single<EmailVerificationResponse> {
        return authApi.resendVerificationEmail(
            RequestToken(
                Email(
                    email
                )
            )
        )
    }

    fun verifyEmail(token: String): Single<EmailVerificationResponse> {
        return authApi.verifyEmail(
            RequestEmailVerification(
                Token(
                    token
                )
            )
        )
    }

    fun resetPassword(requestPasswordReset: RequestPasswordReset): Single<ResetPasswordResponse> {
        return authApi.resetPassword(requestPasswordReset)
    }
}
