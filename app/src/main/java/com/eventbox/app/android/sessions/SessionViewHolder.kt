package com.eventbox.app.android.sessions

import android.graphics.Color
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_session.view.microLocation
import kotlinx.android.synthetic.main.item_session.view.sessionType
import kotlinx.android.synthetic.main.item_session.view.sessiontime
import kotlinx.android.synthetic.main.item_session.view.shortAbstract
import kotlinx.android.synthetic.main.item_session.view.title
import kotlinx.android.synthetic.main.item_session.view.trackDetail
import kotlinx.android.synthetic.main.item_session.view.trackIcon
import kotlinx.android.synthetic.main.item_session.view.trackText
import com.eventbox.app.android.common.SessionClickListener
import com.eventbox.app.android.event.EventUtils
import com.eventbox.app.android.utils.nullToEmpty
import com.eventbox.app.android.utils.stripHtml

class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var sessionClickListener: SessionClickListener? = null

    fun bind(session: Session) {
        itemView.title.text = session.title

        val sessionType = session.sessionType
        if (sessionType == null)
            itemView.sessionType.isVisible = false
        else
            itemView.sessionType.text = sessionType.name

        val microLocation = session.microlocation
        if (microLocation == null)
            itemView.microLocation.isVisible = false
        else
            itemView.microLocation.text = microLocation.name

        session.track.let {
            if (it == null)
                itemView.trackDetail.isVisible = false
            else {
                itemView.trackText.text = it.name
                itemView.trackIcon.setColorFilter(Color.parseColor(it.color))
            }
        }
        when (session.startsAt.isNullOrBlank()) {
            true -> itemView.sessiontime.isVisible = false
            false -> {
                val formattedDateTime = EventUtils.getEventDateTime(session.startsAt)
                val formattedTime = EventUtils.getFormattedTime(formattedDateTime)
                val formattedDate = EventUtils.getFormattedDateShort(formattedDateTime)
                val timezone = EventUtils.getFormattedTimeZone(formattedDateTime)
                itemView.sessiontime.text = "$formattedTime $timezone/ $formattedDate"
            }
        }
        val shortBio = session.shortAbstract.nullToEmpty().stripHtml()
        when (shortBio.isNullOrBlank()) {
            true -> itemView.shortAbstract.isVisible = false
            false -> itemView.shortAbstract.text = shortBio
        }

        itemView.setOnClickListener {
            sessionClickListener?.onClick(session.id)
        }
    }
}
