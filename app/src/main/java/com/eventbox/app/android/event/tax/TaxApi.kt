package com.eventbox.app.android.event.tax

import com.eventbox.app.android.event.tax.Tax
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface TaxApi {

    @GET("events/{event_identifier}/tax?include=event")
    fun getTaxDetails(@Path("event_identifier") identifier: String): Single<Tax>
}
