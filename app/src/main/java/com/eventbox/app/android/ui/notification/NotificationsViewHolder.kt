package com.eventbox.app.android.ui.notification

import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.eventbox.app.android.models.notification.Notification
import kotlinx.android.synthetic.main.item_card_notification.view.*
import com.eventbox.app.android.utils.EventUtils

class NotificationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        notification: Notification

    ) {
        itemView.title.text = notification.title
        itemView.message.movementMethod = LinkMovementMethod.getInstance()
        itemView.message.text = Html.fromHtml(notification.message)
        notification.receivedAt?.let {
            val dayDiff = EventUtils.getDayDifferenceFromToday(it)
            val formattedDateTime = EventUtils.getEventDateTime(it)
            itemView.time.text = when (dayDiff) {
                0L -> EventUtils.getFormattedTime(formattedDateTime)
                in 1..6 -> EventUtils.getFormattedWeekDay(formattedDateTime)
                else -> EventUtils.getFormattedDateWithoutWeekday(formattedDateTime)
            }
        }
    }
}
