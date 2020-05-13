package com.eventbox.app.android.ui.event

import android.os.Build
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.eventbox.app.android.ui.common.EventClickListener
import com.eventbox.app.android.ui.common.FavoriteFabClickListener
import com.eventbox.app.android.databinding.ItemCardFavoriteEventBinding
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.ui.common.InterestedFabClickListener
import com.eventbox.app.android.utils.EventUtils
import kotlinx.android.synthetic.main.item_card_favorite_event.view.*

class FavoriteEventViewHolder(
    private val binding: ItemCardFavoriteEventBinding
) : RecyclerView.ViewHolder(binding.root) {

    var eventClickListener: EventClickListener? = null
    var favFabClickListener: FavoriteFabClickListener? = null
    var interestFabClickListener: InterestedFabClickListener? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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

        itemView.interestFab.scaleType = ImageView.ScaleType.CENTER
        itemView.shareFab.scaleType = ImageView.ScaleType.CENTER
        itemView.favoriteFab.scaleType = ImageView.ScaleType.CENTER
        itemView.eventImage.clipToOutline = true
        itemView.setOnClickListener {
            eventClickListener?.onClick(event.id, itemView.eventImage)
        }
        itemView.shareFab.setOnClickListener {
            EventUtils.share(event, itemView.context)
        }
        itemView.interestFab.setOnClickListener {
            interestFabClickListener?.onClick(event, itemPosition)
        }
        itemView.favoriteFab.setOnClickListener {
            favFabClickListener?.onClick(event, itemPosition)
        }
    }
}
