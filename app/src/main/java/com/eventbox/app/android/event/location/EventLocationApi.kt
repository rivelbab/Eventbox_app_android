package com.eventbox.app.android.event.location

import com.eventbox.app.android.event.location.EventLocation
import io.reactivex.Single
import retrofit2.http.GET

interface EventLocationApi {

    @GET("event-locations?sort=name")
    fun getEventLocation(): Single<List<EventLocation>>
}
