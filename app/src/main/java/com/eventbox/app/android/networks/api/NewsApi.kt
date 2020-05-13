package com.eventbox.app.android.networks.api

import com.eventbox.app.android.models.news.News
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface NewsApi {

    @GET("https://us-central1-eventboxapi.cloudfunctions.net/webApi/eventbox/api/v1/news")
    fun getAllNews() : Single<List<News>>

    @POST("https://us-central1-eventboxapi.cloudfunctions.net/webApi/eventbox/api/v1/news")
    fun createNews(@Body news: News): Single<News>
}