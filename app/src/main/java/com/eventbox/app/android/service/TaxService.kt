package com.eventbox.app.android.service

import com.eventbox.app.android.networks.api.TaxApi
import com.eventbox.app.android.data.dao.TaxDao
import com.eventbox.app.android.models.payment.Tax
import io.reactivex.Single
import timber.log.Timber

class TaxService(
    private val taxApi: TaxApi,
    private val taxDao: TaxDao
) {

    fun getTax(eventId: Long): Single<Tax> {
        return taxApi.getTaxDetails(eventId.toString())
            .onErrorResumeNext {
                Timber.e(it, "Error fetching tax")
                taxDao.getTaxDetails(eventId)
        }.map {
                taxDao.insertTax(it)
                it
            }
    }
}
