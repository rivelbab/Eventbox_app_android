package com.eventbox.app.android.networks.api

import io.reactivex.Single
import com.eventbox.app.android.models.attendees.CustomForm
import com.eventbox.app.android.models.session.Session
import com.eventbox.app.android.models.speakers.Proposal
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SessionApi {

    @GET("events/{eventId}/sessions?include=session-type,microlocation,track,event")
    fun getSessionsForEvent(
        @Path("eventId") eventId: Long,
        @Query("sort") sort: String = "created-at",
        @Query("filter") sessionName: String = "[]"
    ): Single<List<Session>>

    @POST("sessions?include=track,session-type,event,creator,speakers")
    fun createSession(@Body proposal: Proposal): Single<Session>

    @PATCH("sessions/{sessionId}")
    fun updateSession(@Path("sessionId") sessionId: Long, @Body proposal: Proposal): Single<Session>

    @GET("speakers/{speakerId}/sessions?include=session-type,microlocation,track,event")
    fun getSessionsUnderSpeaker(
        @Path("speakerId") speakerId: Long,
        @Query("filter") filter: String
    ): Single<List<Session>>

    @GET("sessions/{sessionId}?include=track,session-type,event,creator,speakers")
    fun getSessionById(@Path("sessionId") sessionId: Long): Single<Session>

    @GET("events/{id}/custom-forms")
    fun getCustomForms(
        @Path("id") eventId: Long,
        @Query("filter") filter: String
    ): Single<List<CustomForm>>
}
