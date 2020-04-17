package com.eventbox.app.android.event.types

import com.eventbox.app.android.event.types.EventType
import io.reactivex.Single
import retrofit2.http.GET

interface EventTypesApi {

    @GET("event-types?sort=name")
    fun getEventTypes(): Single<List<EventType>>
}
