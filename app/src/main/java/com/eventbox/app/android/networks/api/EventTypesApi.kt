package com.eventbox.app.android.networks.api

import com.eventbox.app.android.models.event.EventType
import io.reactivex.Single
import retrofit2.http.GET

interface EventTypesApi {

    @GET("event-types?sort=name")
    fun getEventTypes(): Single<List<EventType>>
}
