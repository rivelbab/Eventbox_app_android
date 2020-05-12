package com.eventbox.app.android.ui.news

import android.os.Build
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.eventbox.app.android.databinding.ItemCardNewsBinding
import com.eventbox.app.android.models.news.News
import com.eventbox.app.android.ui.common.NewsClickListener
import com.eventbox.app.android.utils.EventUtils
import com.eventbox.app.android.utils.NewsUtils
import kotlinx.android.synthetic.main.item_card_news.view.*

class NewsViewHolder (private val binding: ItemCardNewsBinding): RecyclerView.ViewHolder(binding.root) {

    var newsClickListener: NewsClickListener? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun bind(news: News, itemPosition: Int, headerDate: String) {
        val publishedOn = EventUtils.getEventDateTime(news.publishedOn.toString(), "UTC")

        with(binding) {
            this.news = news
            this.headerDate = headerDate
            position = itemPosition
            dateTime = EventUtils.getFormattedTime(publishedOn)
            executePendingBindings()
        }

        itemView.shareFab.scaleType = ImageView.ScaleType.CENTER
        itemView.newsImage.clipToOutline = true
        itemView.setOnClickListener {
            newsClickListener?.onClick(news.id.toString(), itemView.newsImage)
        }
        itemView.shareFab.setOnClickListener {
            NewsUtils.share(news, itemView.context)
        }
    }
}