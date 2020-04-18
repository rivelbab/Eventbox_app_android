package com.eventbox.app.android.networks.api

import com.eventbox.app.android.models.event.SocialLink
import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Path

interface SocialLinkApi {

    @GET("events/{id}/social-links?include=event&fields[event]=id&page[size]=0")
    fun getSocialLinks(@Path("id") id: Long): Flowable<List<SocialLink>>
}
