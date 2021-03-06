package com.eventbox.app.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.ListAdapter
import com.eventbox.app.android.ui.common.EventClickListener
import com.eventbox.app.android.ui.common.EventsDiffCallback
import com.eventbox.app.android.ui.common.FavoriteFabClickListener
import com.eventbox.app.android.databinding.ItemCardEventsBinding
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.ui.event.EventViewHolder

/**
 * The RecyclerView adapter class for displaying lists of Events.
 *
 * @param diffCallback The DiffUtil.ItemCallback implementation to be used with this adapter
 * @property onEventClick The callback to be invoked when an event is clicked
 * @property onFavFabClick The callback to be invoked when the favorite FAB is clicked
 */
class EventsListAdapter : ListAdapter<Event, EventViewHolder>(EventsDiffCallback()) {

    var onEventClick: EventClickListener? = null
    var onFavFabClick: FavoriteFabClickListener? = null
    var onHashtagClick: EventHashTagClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemCardEventsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        if (event != null)
            holder.apply {
                bind(event, position)
                eventClickListener = onEventClick
                favFabClickListener = onFavFabClick
                hashTagClickListAdapter = onHashtagClick
            }
    }

    /**
     * The function to call when the adapter has to be cleared of items
     */
    fun clear() {
        this.submitList(null)
    }
}

interface EventHashTagClickListener {
    fun onClick(hashTagValue: String)
}
