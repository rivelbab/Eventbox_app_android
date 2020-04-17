package com.eventbox.app.android.event.faq

import com.eventbox.app.android.event.faq.EventFAQ
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface EventFAQApi {

    @GET("events/{id}/faqs?sort=question")
    fun getEventFAQ(@Path("id") id: Long): Single<List<EventFAQ>>
}
