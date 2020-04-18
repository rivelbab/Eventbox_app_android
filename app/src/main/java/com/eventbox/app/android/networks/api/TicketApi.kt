package com.eventbox.app.android.networks.api

import com.eventbox.app.android.models.payment.Ticket
import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Path

interface TicketApi {

    @GET("events/{id}/tickets?include=event&fields[event]=id&page[size]=0")
    fun getTickets(@Path("id") id: Long): Flowable<List<Ticket>>
}
