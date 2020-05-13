package com.eventbox.app.android.fragments.news

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.eventbox.app.android.R
import com.eventbox.app.android.databinding.FragmentNewsDetailBinding
import com.eventbox.app.android.models.news.News
import com.eventbox.app.android.ui.news.NewsDetailViewModel
import com.eventbox.app.android.utils.Utils.progressDialog
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.Utils.show
import com.eventbox.app.android.utils.extensions.nonNull
import kotlinx.android.synthetic.main.content_fetching_news_error.view.*
import kotlinx.android.synthetic.main.fragment_news_detail.view.*
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class NewsDetailFragment : Fragment() {

    private val newsDetailViewModel by viewModel<NewsDetailViewModel>()
    private val safeArgs: NewsDetailFragmentArgs by navArgs()
    private lateinit var rootView: View
    private lateinit var binding: FragmentNewsDetailBinding
    private var currentNews: News? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_news_detail, container, false)
        val progressDialog = progressDialog(context, getString(R.string.loading_message))
        rootView = binding.root
        setToolbar(activity)

        setupNewsOverview()

        newsDetailViewModel.popMessage
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
                showNewsErrorScreen(it == getString(R.string.fetch_news_error_message))
            })

        newsDetailViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        rootView.retry.setOnClickListener {
            currentNews?.let { newsDetailViewModel.loadNewsById(it.id.toString()) }
        }

        return rootView
    }

    private fun showNewsErrorScreen(show: Boolean) {
        rootView.container.isVisible = !show
        rootView.newsErrorCard.isVisible = show
    }

    private fun setupNewsOverview() {
        newsDetailViewModel.news
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                currentNews = it
                loadNews(it)
                Timber.d("Fetched news of id ${it.id}")
                showNewsErrorScreen(false)
            })

        val news = newsDetailViewModel.news.value
        when {
            news != null -> {
                currentNews = news
                loadNews(news)
            }
            else -> newsDetailViewModel.loadNewsById(safeArgs.newsId)
        }
    }

    private fun loadNews(news: News) {

        binding.news = news
        binding.executePendingBindings()
    }
}