package com.eventbox.app.android.fragments.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.eventbox.app.android.ui.common.NewsClickListener
import com.eventbox.app.android.ui.news.NewsViewModel
import com.eventbox.app.android.utils.EventUtils.getEventDateTime
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.extensions.hideWithFading
import com.eventbox.app.android.utils.extensions.nonNull
import com.eventbox.app.android.utils.extensions.showWithFading
import kotlinx.android.synthetic.main.fragment_news.view.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val NEWS_FRAGMENT = "newsFragment"

class NewsFragment : Fragment(), BottomIconDoubleClick {
    
    private val newsViewModel by viewModel<NewsViewModel>()
    private lateinit var rootView: View
    private val newsRecyclerAdapter = NewsListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        //setPostponeSharedElementTransition()
        rootView = inflater.inflate(R.layout.fragment_news, container, false)

        rootView.newsRecycler.layoutManager = LinearLayoutManager(activity)
        rootView.newsRecycler.adapter = newsRecyclerAdapter
        rootView.newsRecycler.isNestedScrollingEnabled = false
        rootView.viewTreeObserver.addOnDrawListener {
            //setStartPostponedEnterTransition()
        }

        newsViewModel.news
            .nonNull()
            .observe(viewLifecycleOwner, Observer { list ->
                newsRecyclerAdapter.submitList(list.sortedBy { getEventDateTime(it.publishedOn, "UTC") })
                rootView.newsNumber.text = resources.getQuantityString(R.plurals.events_number, list.size, list.size)
                showEmptyMessage(list.size)
                Timber.d("Fetched news of size %s", list.size)
            })

        newsViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.longSnackbar(it)
            })

        newsViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.newsProgressBar.isIndeterminate = it
                rootView.newsProgressBar.isVisible = it
            })

        newsViewModel.loadAllNews()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsClickListener: NewsClickListener = object : NewsClickListener {
            override fun onClick(newsID: String, imageView: ImageView) {

            }
        }

        newsRecyclerAdapter.apply {
            onNewsClick = newsClickListener
        }

        rootView.scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY > rootView.newsTitle.y && !rootView.toolbarLayout.isVisible)
                rootView.toolbarLayout.showWithFading()
            else if (scrollY < rootView.newsTitle.y && rootView.toolbarLayout.isVisible)
                rootView.toolbarLayout.hideWithFading()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        newsRecyclerAdapter.apply {
            onNewsClick = null
        }
    }

    override fun onResume() {
        super.onResume()
        setToolbar(activity, show = false)
    }

    override fun doubleClick() = rootView.scrollView.smoothScrollTo(0, 0)

    private fun showEmptyMessage(itemCount: Int) {
        rootView.noNewsLL.isVisible = (itemCount == 0)
    }
}
