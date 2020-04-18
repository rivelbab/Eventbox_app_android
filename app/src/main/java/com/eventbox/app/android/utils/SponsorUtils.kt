package com.eventbox.app.android.utils

import com.eventbox.app.android.models.sponsor.Sponsor

object SponsorUtils {
    fun sortSponsorByLevel(sponsors: List<Sponsor>): List<Sponsor> {
        return sponsors.sortedWith(compareBy(Sponsor::level))
    }
}
