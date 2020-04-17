package com.eventbox.app.android.event.tax

import com.eventbox.app.android.event.tax.Tax
import com.eventbox.app.android.event.tax.TaxApi
import com.eventbox.app.android.event.tax.TaxDao
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
