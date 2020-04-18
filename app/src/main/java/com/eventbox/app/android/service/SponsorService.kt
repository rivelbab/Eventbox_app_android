package com.eventbox.app.android.service

import com.eventbox.app.android.data.dao.SponsorDao
import com.eventbox.app.android.models.sponsor.Sponsor
import com.eventbox.app.android.networks.api.SponsorApi
import com.eventbox.app.android.models.sponsor.SponsorWithEvent
import com.eventbox.app.android.data.dao.SponsorWithEventDao
import io.reactivex.Single

class SponsorService(
    private val sponsorApi: SponsorApi,
    private val sponsorDao: SponsorDao,
    private val sponsorWithEventDao: SponsorWithEventDao
) {
    fun fetchSponsorsWithEvent(id: Long): Single<List<Sponsor>> {
        return sponsorApi.getSponsorWithEvent(id).doOnSuccess { sponsors ->
            sponsors.forEach { sponsor ->
                sponsorDao.insertSponsor(sponsor)
                sponsorWithEventDao.insert(
                    SponsorWithEvent(
                        id,
                        sponsor.id
                    )
                )
            }
        }
    }
}
