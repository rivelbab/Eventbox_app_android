package com.eventbox.app.android.networks.api

import com.eventbox.app.android.models.news.News
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NewsApi {

    @GET("news")
    fun getAllNews() : Single<List<News>>

    @POST("news")
    fun createNews(@Body news: News): Single<News>

    @GET("news/{newsId}")
    fun getNewsById(@Path("newsId") newsId: String) : Single<News>
}