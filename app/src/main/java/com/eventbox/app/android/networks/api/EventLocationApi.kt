package com.eventbox.app.android.networks.api

import com.eventbox.app.android.models.event.EventLocation
import io.reactivex.Single
import retrofit2.http.GET

interface EventLocationApi {

    @GET("event-locations?sort=name")
    fun getEventLocation(): Single<List<EventLocation>>
}
