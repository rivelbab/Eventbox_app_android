package com.eventbox.app.android.networks.api

import io.reactivex.Completable
import io.reactivex.Single
import com.eventbox.app.android.networks.payloads.auth.ChangeRequestToken
import com.eventbox.app.android.networks.payloads.auth.ChangeRequestTokenResponse
import com.eventbox.app.android.models.auth.Email
import com.eventbox.app.android.networks.payloads.auth.RequestToken
import com.eventbox.app.android.networks.payloads.auth.RequestTokenResponse
import com.eventbox.app.android.models.auth.Login
import com.eventbox.app.android.models.auth.SignUp
import com.eventbox.app.android.models.user.User
import com.eventbox.app.android.networks.payloads.auth.*
import com.eventbox.app.android.networks.payloads.utils.ImageResponse
import com.eventbox.app.android.networks.payloads.utils.UploadImage
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {

    @POST("login")
    fun login(@Body login: Login): Single<LoginResponse>

    @GET("users/{id}")
    fun getProfile(@Path("id") id: String): Single<User>

    @POST("signup")
    fun signUp(@Body signUp: SignUp): Single<User>

    @POST("auth/reset-password")
    fun requestToken(@Body requestToken: RequestToken): Single<RequestTokenResponse>

    @POST("auth/change-password")
    fun changeRequestToken(@Body changeRequestToken: ChangeRequestToken): Single<ChangeRequestTokenResponse>

    @PATCH("users/{id}")
    fun updateUser(@Body user: User, @Path("id") id: String): Single<User>

    @POST("upload/image")
    fun uploadImage(@Body uploadImage: UploadImage): Single<ImageResponse>

    @POST("users/checkEmail")
    fun checkEmail(@Body email: Email): Single<CheckEmailResponse>

    @POST("auth/resend-verification-email")
    fun resendVerificationEmail(@Body requestToken: RequestToken): Single<EmailVerificationResponse>

    @POST("auth/verify-email")
    fun verifyEmail(@Body requestEmailVerification: RequestEmailVerification): Single<EmailVerificationResponse>

    @PATCH("auth/reset-password")
    fun resetPassword(@Body requestPasswordReset: RequestPasswordReset): Single<ResetPasswordResponse>

    @DELETE("users/{id}")
    fun deleteAccount(@Path("id") userId: String): Completable
}
