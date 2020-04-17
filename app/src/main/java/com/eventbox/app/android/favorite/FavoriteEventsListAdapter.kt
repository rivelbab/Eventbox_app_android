package com.eventbox.app.android.favorite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.eventbox.app.android.common.EventClickListener
import com.eventbox.app.android.common.EventsDiffCallback
import com.eventbox.app.android.common.FavoriteFabClickListener
import com.eventbox.app.android.databinding.ItemCardFavoriteEventBinding
import com.eventbox.app.android.event.Event
import com.eventbox.app.android.event.EventUtils.getEventDateTime
import com.eventbox.app.android.event.EventUtils.getFormattedDate
import com.eventbox.app.android.favorite.FavoriteEventViewHolder

/**
 * The RecyclerView adapter class for displaying favorite events list
 *
 * @param diffCallback The DiffUtil.ItemCallback implementation to be used with this adapter.
 * @property onEventClick The callback to be invoked when an event is clicked
 * @property onFavFabClick The callback to be invoked when the favorite FAB is clicked
 * @property onShareFabClick The callback to be invoked when the share FAB is clicked
 */
class FavoriteEventsListAdapter : ListAdapter<Event, FavoriteEventViewHolder>(EventsDiffCallback()) {

    var onEventClick: EventClickListener? = null
    var onFavFabClick: FavoriteFabClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteEventViewHolder {
        val binding = ItemCardFavoriteEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteEventViewHolder, position: Int) {
        val event = getItem(position)
        holder.apply {
            val eventDate = getDateFormat(event.startsAt, event.timezone)
            var showEventDate = true
            if (position != 0) {
                val previousEvent = getItem(position - 1)
                if (previousEvent != null && eventDate == getDateFormat(previousEvent.startsAt, previousEvent.timezone))
                    showEventDate = false
            }
            bind(event, position, if (showEventDate) eventDate else "")
            eventClickListener = onEventClick
            favFabClickListener = onFavFabClick
        }
    }

    private fun getDateFormat(eventDate: String, timeZone: String): String {
        val date = getEventDateTime(eventDate, timeZone)
        return getFormattedDate(date)
    }
}
