package com.eventbox.app.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import com.eventbox.app.android.ui.common.EventClickListener
import com.eventbox.app.android.ui.common.EventsDiffCallback
import com.eventbox.app.android.ui.common.FavoriteFabClickListener
import com.eventbox.app.android.databinding.ItemCardFavoriteEventBinding
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.ui.event.FavoriteEventViewHolder

class SearchPagedListAdapter : PagedListAdapter<Event, FavoriteEventViewHolder>(EventsDiffCallback()) {

    var onEventClick: EventClickListener? = null
    var onFavFabClick: FavoriteFabClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteEventViewHolder {
        val binding = ItemCardFavoriteEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteEventViewHolder, position: Int) {
        val event = getItem(position)
        if (event != null) {
            holder.apply {
                bind(event, position, "")
                eventClickListener = onEventClick
                favFabClickListener = onFavFabClick
            }
        }
    }

    fun clear() {
        this.submitList(null)
    }
}
