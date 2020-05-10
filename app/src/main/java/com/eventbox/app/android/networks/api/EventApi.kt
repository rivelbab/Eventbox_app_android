package com.eventbox.app.android.networks.api

import com.eventbox.app.android.models.event.Event
import io.reactivex.Single
import retrofit2.http.*

interface EventApi {

    @GET
    fun getEvent(id: Long): Single<Event>

    @GET("/v1/events/{eventIdentifier}")
    fun getEventFromApi(@Path("eventIdentifier") eventIdentifier: String): Single<Event>

    @POST("events")
    fun createEvent(@Body event: Event): Single<Event>

    @GET("events")
    fun eventsWithQuery(@Query("filter") filter: String): Single<List<Event>>

    @GET("events?include=event-sub-topic,event-topic,event-type")
    fun searchEventsPaged(
        @Query("sort") sort: String,
        @Query("filter") eventName: String,
        @Query("page[number]") page: Int,
        @Query("page[size]") pageSize: Int = 5
    ): Single<List<Event>>

    @GET("events")
    fun eventsByQuery(@Query("filter") filter: String): Single<List<Event>>

    @GET("event-topics/{id}/events?include=event-topic")
    fun getEventsUnderTypeIdPaged(
        @Path("id") id: Long,
        @Query("filter") filter: String,
        @Query("page[number]") page: Int,
        @Query("page[size]") pageSize: Int = 5
    ): Single<List<Event>>
}
