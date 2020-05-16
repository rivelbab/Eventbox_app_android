package com.eventbox.app.android.networks.api

import com.eventbox.app.android.models.event.EventFAQ
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface EventFAQApi {

    @GET("events/{id}/faqs?sort=question")
    fun getEventFAQ(@Path("id") id: String): Single<List<EventFAQ>>
}
