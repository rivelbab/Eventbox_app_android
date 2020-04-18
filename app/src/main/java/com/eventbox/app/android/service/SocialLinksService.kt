package com.eventbox.app.android.service

import com.eventbox.app.android.data.dao.SocialLinksDao
import com.eventbox.app.android.models.event.SocialLink
import com.eventbox.app.android.networks.api.SocialLinkApi
import io.reactivex.Flowable

class SocialLinksService(
    private val socialLinkApi: SocialLinkApi,
    private val socialLinksDao: SocialLinksDao
) {

    fun getSocialLinks(id: Long): Flowable<List<SocialLink>> {

        val socialFlowable = socialLinksDao.getAllSocialLinks(id)
        return socialFlowable.switchMap {
            if (it.isNotEmpty())
                socialFlowable
            else
                socialLinkApi.getSocialLinks(id)
                        .map {
                            socialLinksDao.insertSocialLinks(it)
                        }
                        .flatMap {
                            socialFlowable
                        }
        }
    }
}
