package com.eventbox.app.android.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eventbox.app.android.R
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.models.news.News
import com.eventbox.app.android.networks.connectivity.MutableConnectionLiveData
import com.eventbox.app.android.service.AuthService
import com.eventbox.app.android.service.NewsService
import com.eventbox.app.android.ui.auth.AuthHolder
import com.eventbox.app.android.ui.common.SingleLiveEvent
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

class NewsDetailViewModel(
    private val newsService: NewsService,
    private val authHolder: AuthHolder,
    private val authService: AuthService,
    private val resource: Resource,
    private val mutableConnectionLiveData: MutableConnectionLiveData
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val connection: LiveData<Boolean> = mutableConnectionLiveData
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutablePopMessage = SingleLiveEvent<String>()
    val popMessage: SingleLiveEvent<String> = mutablePopMessage
    private val mutableNews = MutableLiveData<News>()
    val news: LiveData<News> = mutableNews

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun getId() = authHolder.getId()

    fun getLoggedInUserName(): String {
        var username: String = ""
        compositeDisposable += authService.getProfile()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({ user ->
                username = user.firstName.toString() + " " + user.lastName.toString()
            }) {
                Timber.e(it, "Failure")
                mutablePopMessage.value = resource.getString(R.string.failure)
            }
        return username
    }

    fun loadNewsById(newsId: String) {
        if (newsId.isEmpty()) {
            mutablePopMessage.value = resource.getString(R.string.fetch_news_error_message)
            return
        }
        compositeDisposable += newsService.getNewsById(newsId)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableNews.value = it
            }, {
                Timber.e(it, "Error fetching news")
                mutablePopMessage.value = resource.getString(R.string.fetch_news_error_message)
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}