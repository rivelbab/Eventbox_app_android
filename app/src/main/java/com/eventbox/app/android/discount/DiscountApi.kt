package com.eventbox.app.android.discount

import io.reactivex.Single
import com.eventbox.app.android.discount.DiscountCode
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DiscountApi {

    @GET("events/{eventId}/discount-codes/{code}?include=event,tickets")
    fun getDiscountCodes(
        @Path("eventId") eventId: Long,
        @Path("code") code: String,
        @Query("filter") filter: String
    ): Single<DiscountCode>
}
