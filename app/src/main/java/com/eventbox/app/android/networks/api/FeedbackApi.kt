package com.eventbox.app.android.networks.api

import com.eventbox.app.android.models.feedback.Feedback
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedbackApi {

    @GET("events/{eventId}/feedbacks")
    fun getEventFeedback(
        @Path("eventId") eventId: String
    ): Single<List<Feedback>>

    @POST("feedbacks")
    fun postfeedback(@Body feedback: Feedback): Single<Feedback>
}
