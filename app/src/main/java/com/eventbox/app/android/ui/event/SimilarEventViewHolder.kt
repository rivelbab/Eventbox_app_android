package com.eventbox.app.android.ui.event

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_card_events.view.eventImage
import kotlinx.android.synthetic.main.item_card_events.view.favoriteFab
import kotlinx.android.synthetic.main.item_card_events.view.shareFab
import com.eventbox.app.android.ui.common.EventClickListener
import com.eventbox.app.android.ui.common.FavoriteFabClickListener
import com.eventbox.app.android.databinding.ItemCardSimilarEventsBinding
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.utils.EventUtils

class SimilarEventViewHolder(
    private val binding: ItemCardSimilarEventsBinding
) : RecyclerView.ViewHolder(binding.root) {

    var eventClickListener: EventClickListener? = null
    var favFabClickListener: FavoriteFabClickListener? = null

    fun bind(
        event: Event,
        itemPosition: Int
    ) {
        val time = EventUtils.getEventDateTime(event.startsAt, event.timezone)

        with(binding) {
            this.event = event
            position = itemPosition
            monthTime = time.month.name.slice(0 until 3)
            dateTime = time.dayOfMonth.toString()
            executePendingBindings()
        }

        itemView.shareFab.scaleType = ImageView.ScaleType.CENTER
        itemView.favoriteFab.scaleType = ImageView.ScaleType.CENTER

        itemView.setOnClickListener {
            eventClickListener?.onClick(event.id, itemView.eventImage)
        }
        itemView.shareFab.setOnClickListener {
            EventUtils.share(event, itemView.context)
        }
        itemView.favoriteFab.setOnClickListener {
            favFabClickListener?.onClick(event, itemPosition)
        }
    }
}
