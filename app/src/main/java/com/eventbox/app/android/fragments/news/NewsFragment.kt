package com.eventbox.app.android.fragments.news

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import com.eventbox.app.android.BottomIconDoubleClick
import com.eventbox.app.android.R
import com.eventbox.app.android.adapters.NewsListAdapter
import com.eventbox.app.android.models.news.News
import com.eventbox.app.android.ui.common.NewsClickListener
import com.eventbox.app.android.ui.news.NewsViewModel
import com.eventbox.app.android.utils.EventUtils.getEventDateTime
import com.eventbox.app.android.utils.Utils.progressDialog
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.extensions.hideWithFading
import com.eventbox.app.android.utils.extensions.nonNull
import com.eventbox.app.android.utils.extensions.showWithFading
import com.eventbox.app.android.utils.nullToEmpty
import kotlinx.android.synthetic.main.content_no_internet.view.*
import kotlinx.android.synthetic.main.fragment_news.view.*
import kotlinx.android.synthetic.main.item_card_news.view.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class NewsFragment : Fragment(), BottomIconDoubleClick {
    
    private val newsViewModel by viewModel<NewsViewModel>()
    private lateinit var rootView: View
    private val newsListAdapter = NewsListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        //setPostponeSharedElementTransition()
        rootView = inflater.inflate(R.layout.fragment_news, container, false)

        setToolbar(activity, show = false)

        rootView.newsRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.newsRecycler.adapter = newsListAdapter
        rootView.newsRecycler.isNestedScrollingEnabled = false

        newsViewModel.news
            .nonNull()
            .observe(viewLifecycleOwner, Observer { list ->
                newsListAdapter.submitList(list.sortedBy { getEventDateTime(it.publishedOn.toString(), "UTC") })
                rootView.newsNumber.text = resources.getQuantityString(R.plurals.news_number, list.size, list.size)
                if (!rootView.shimmerNews.isVisible)
                    showEmptyMessage(newsListAdapter.currentList.isEmpty() ?: true)
                Timber.d("Fetched news of size %s", list.size)
            })

        newsViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it) {
                    rootView.shimmerNews.startShimmer()
                    showEmptyMessage(false)
                    showNoInternetScreen(false)
                } else {
                    rootView.shimmerNews.stopShimmer()
                    rootView.swipeRefresh.isRefreshing = false
                }
                rootView.shimmerNews.isVisible = it
            })


        newsViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.longSnackbar(it)
            })

        newsViewModel.connection
            .nonNull()
            .observe(viewLifecycleOwner, Observer { isConnected ->
                val currentNews = newsViewModel.news.value
                if (currentNews != null) {
                    showNoInternetScreen(false)
                    newsListAdapter.submitList(currentNews)
                } else {
                    if (isConnected) {
                        newsViewModel.loadAllNews()
                    } else {
                        showNoInternetScreen(true)
                    }
                }
            })

        rootView.retry.setOnClickListener {
            if (newsViewModel.isConnected()) {
                newsViewModel.loadAllNews()
            }
            showNoInternetScreen(!newsViewModel.isConnected())
        }

        rootView.swipeRefresh.setColorSchemeColors(Color.BLUE)
        rootView.swipeRefresh.setOnRefreshListener {
            showNoInternetScreen(!newsViewModel.isConnected())
            newsViewModel.clearNews()
            if (!newsViewModel.isConnected()) {
                rootView.swipeRefresh.isRefreshing = false
            } else {
                newsViewModel.loadAllNews()
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsClickListener: NewsClickListener = object : NewsClickListener {
            override fun onClick(newsID: String) {
                findNavController(rootView).navigate(
                    NewsFragmentDirections.actionNewsToNewsDetail(newsID)
                )
            }
        }

       newsListAdapter.apply {
            onNewsClick = newsClickListener
        }

        rootView.scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY > rootView.newsTitle.y && !rootView.toolbarLayout.isVisible)
                rootView.toolbarLayout.showWithFading()
            else if (scrollY < rootView.newsTitle.y && rootView.toolbarLayout.isVisible)
                rootView.toolbarLayout.hideWithFading()
        }
    }

    private fun showEmptyMessage(show: Boolean) {
        rootView.newsEmptyView.isVisible = show
    }

    private fun showNoInternetScreen(show: Boolean) {
        if(show) {
            rootView.shimmerNews.isVisible = false
            rootView.newsEmptyView.isVisible = false
            newsListAdapter.clear()
        }
        rootView.noInternetCard.isVisible = show
    }

    override fun onDestroyView() {
        super.onDestroyView()
       newsListAdapter.apply {
            onNewsClick = null
        }
    }

    override fun onResume() {
        super.onResume()
        setToolbar(activity, show = false)
    }

    override fun doubleClick() = rootView.scrollView.smoothScrollTo(0, 0)
}
