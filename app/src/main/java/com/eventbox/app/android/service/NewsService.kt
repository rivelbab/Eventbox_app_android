package com.eventbox.app.android.service

import com.eventbox.app.android.models.news.News
import com.eventbox.app.android.networks.api.NewsApi
import io.reactivex.Single

class NewsService(
    private val newsApi: NewsApi
) {

    fun createNews(news: News): Single<News> {
        return newsApi.createNews(news)
    }

    fun loadAllNews(): Single<List<News>> = newsApi.getAllNews()

    fun getNewsById(newsId: String): Single<News> {
        return newsApi.getNewsById(newsId)
    }
}