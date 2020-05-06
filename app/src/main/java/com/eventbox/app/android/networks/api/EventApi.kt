package com.eventbox.app.android.networks.api

import com.eventbox.app.android.models.event.Event
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EventApi {

    @GET
    fun getEvent(id: Long): Single<Event>

    @GET("/v1/events/{eventIdentifier}")
    fun getEventFromApi(@Path("eventIdentifier") eventIdentifier: String): Single<Event>

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
}
