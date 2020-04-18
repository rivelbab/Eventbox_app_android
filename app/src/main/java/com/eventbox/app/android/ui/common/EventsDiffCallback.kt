package com.eventbox.app.android.ui.common

import androidx.recyclerview.widget.DiffUtil
import com.eventbox.app.android.models.event.Event

/**
 * The DiffUtil ItemCallback class for the [Event] model class.
 * This enables proper diffing of items in Recycler Views using [DiffUtil]
 */
class EventsDiffCallback : DiffUtil.ItemCallback<Event>() {

    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}
