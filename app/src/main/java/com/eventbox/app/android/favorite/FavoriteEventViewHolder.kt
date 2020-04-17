package com.eventbox.app.android.favorite

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_card_favorite_event.view.eventImage
import kotlinx.android.synthetic.main.item_card_favorite_event.view.favoriteFab
import kotlinx.android.synthetic.main.item_card_favorite_event.view.shareFab
import com.eventbox.app.android.common.EventClickListener
import com.eventbox.app.android.common.FavoriteFabClickListener
import com.eventbox.app.android.databinding.ItemCardFavoriteEventBinding
import com.eventbox.app.android.event.Event
import com.eventbox.app.android.event.EventUtils

class FavoriteEventViewHolder(
    private val binding: ItemCardFavoriteEventBinding
) : RecyclerView.ViewHolder(binding.root) {

    var eventClickListener: EventClickListener? = null
    var favFabClickListener: FavoriteFabClickListener? = null

    fun bind(event: Event, itemPosition: Int, headerDate: String) {
        val startsAt = EventUtils.getEventDateTime(event.startsAt, event.timezone)
        val endsAt = EventUtils.getEventDateTime(event.endsAt, event.timezone)

        with(binding) {
            this.event = event
            this.headerDate = headerDate
            position = itemPosition
            dateTime = EventUtils.getFormattedDateTimeRangeBulleted(startsAt, endsAt)
            executePendingBindings()
        }

        itemView.shareFab.scaleType = ImageView.ScaleType.CENTER
        itemView.favoriteFab.scaleType = ImageView.ScaleType.CENTER
        itemView.eventImage.clipToOutline = true
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
