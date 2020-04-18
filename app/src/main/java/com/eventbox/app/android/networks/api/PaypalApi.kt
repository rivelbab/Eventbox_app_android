package com.eventbox.app.android.networks.api

import com.eventbox.app.android.models.payment.Paypal
import com.eventbox.app.android.networks.payloads.payment.PaypalPaymentResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface PaypalApi {

    @POST("orders/{orderIdentifier}/verify-mobile-paypal-payment")
    fun verifyPaypalPayment(
        @Path("orderIdentifier") orderIdentifier: String,
        @Body paypal: Paypal
    ): Single<PaypalPaymentResponse>
}
