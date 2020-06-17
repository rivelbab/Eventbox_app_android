package com.eventbox.app.android.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eventbox.app.android.R
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.models.news.News
import com.eventbox.app.android.networks.connectivity.MutableConnectionLiveData
import com.eventbox.app.android.service.NewsService
import com.eventbox.app.android.ui.auth.AuthHolder
import com.eventbox.app.android.ui.common.SingleLiveEvent
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

class NewsViewModel(
    private val newsService: NewsService,
    private val resource: Resource,
    private val authHolder: AuthHolder,
    private val mutableConnectionLiveData: MutableConnectionLiveData
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val connection: LiveData<Boolean> = mutableConnectionLiveData
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableMessage = SingleLiveEvent<String>()
    val message: SingleLiveEvent<String> = mutableMessage
    private val mutableNews = MutableLiveData<List<News>>()
    val news: LiveData<List<News>> = mutableNews

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun isConnected(): Boolean = mutableConnectionLiveData.value ?: false

    fun loadAllNews() {
        compositeDisposable += newsService.loadAllNews()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }
            .subscribe({
                mutableNews.value = it
                mutableProgress.value = false
            }, {
                Timber.e(it, "Error fetching news")
                mutableMessage.value = resource.getString(R.string.fetch_news_error_message)
            })
    }

    fun clearNews() {
        mutableNews.value = null
    }
}