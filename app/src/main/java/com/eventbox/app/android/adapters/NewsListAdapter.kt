package com.eventbox.app.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.eventbox.app.android.databinding.ItemCardNewsBinding
import com.eventbox.app.android.models.news.News
import com.eventbox.app.android.ui.common.NewsClickListener
import com.eventbox.app.android.ui.common.NewsDiffCallback
import com.eventbox.app.android.ui.news.NewsViewHolder
import com.eventbox.app.android.utils.EventUtils.getEventDateTime
import com.eventbox.app.android.utils.EventUtils.getFormattedDate

class NewsListAdapter : ListAdapter<News, NewsViewHolder>(NewsDiffCallback()) {

    var onNewsClick: NewsClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemCardNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = getItem(position)
        holder.apply {
            val newsDate = getDateFormat(news.publishedOn)
            var showNewsDate = true
            if (position != 0) {
                val previousNews = getItem(position - 1)
                if (previousNews != null && newsDate == getDateFormat(previousNews.publishedOn))
                    showNewsDate = false
            }
            bind(news, position, if (showNewsDate) newsDate else "")
            newsClickListener = onNewsClick
        }
    }

    private fun getDateFormat(newsDate: String, timeZone: String = "UTC"): String {
        val date = getEventDateTime(newsDate, timeZone)
        return getFormattedDate(date)
    }
}