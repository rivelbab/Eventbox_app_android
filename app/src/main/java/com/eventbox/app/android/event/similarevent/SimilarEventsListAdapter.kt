package com.eventbox.app.android.event.similarevent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import com.eventbox.app.android.common.EventClickListener
import com.eventbox.app.android.common.EventsDiffCallback
import com.eventbox.app.android.common.FavoriteFabClickListener
import com.eventbox.app.android.databinding.ItemCardSimilarEventsBinding
import com.eventbox.app.android.event.Event
import com.eventbox.app.android.event.similarevent.SimilarEventViewHolder

class SimilarEventsListAdapter : PagedListAdapter<Event, SimilarEventViewHolder>(EventsDiffCallback()) {

    var onEventClick: EventClickListener? = null
    var onFavFabClick: FavoriteFabClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimilarEventViewHolder {
        val binding = ItemCardSimilarEventsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SimilarEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SimilarEventViewHolder, position: Int) {
        val event = getItem(position)
        if (event != null) {
            holder.apply {
                bind(event, position)
                eventClickListener = onEventClick
                favFabClickListener = onFavFabClick
            }
        }
    }

    fun clear() {
        this.submitList(null)
    }
}
