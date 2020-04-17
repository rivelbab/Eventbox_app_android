package com.eventbox.app.android.sponsor

object SponsorUtil {
    fun sortSponsorByLevel(sponsors: List<Sponsor>): List<Sponsor> {
        return sponsors.sortedWith(compareBy(Sponsor::level))
    }
}
