package com.eventbox.app.android.ui.common

import androidx.recyclerview.widget.DiffUtil
import com.eventbox.app.android.models.news.News

/**
 * The DiffUtil ItemCallback class for the [News] model class.
 * This enables proper diffing of items in Recycler Views using [DiffUtil]
 */
class NewsDiffCallback : DiffUtil.ItemCallback<News>() {

    override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
        return oldItem == newItem
    }
}
