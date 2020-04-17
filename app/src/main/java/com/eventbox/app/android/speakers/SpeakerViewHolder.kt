package com.eventbox.app.android.speakers

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_speaker.view.shortBioTv
import kotlinx.android.synthetic.main.item_speaker.view.speakerImgView
import kotlinx.android.synthetic.main.item_speaker.view.speakerNameTv
import kotlinx.android.synthetic.main.item_speaker.view.speakerOrgTv
import com.eventbox.app.android.CircleTransform
import com.eventbox.app.android.R
import com.eventbox.app.android.common.SpeakerClickListener
import com.eventbox.app.android.utils.Utils
import com.eventbox.app.android.utils.nullToEmpty
import com.eventbox.app.android.utils.stripHtml
import timber.log.Timber

class SpeakerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var speakerClickListener: SpeakerClickListener? = null

    fun bind(speaker: Speaker) {
        itemView.speakerNameTv.text = speaker.name
        itemView.speakerOrgTv.text = speaker.organisation
        val shortBio = speaker.shortBiography.nullToEmpty().stripHtml()
        when (shortBio.isNullOrBlank()) {
            true -> itemView.shortBioTv.isVisible = false
            false -> itemView.shortBioTv.text = shortBio
        }

        Picasso.get()
            .load(speaker.photoUrl)
            .placeholder(Utils.requireDrawable(itemView.context, R.drawable.ic_account_circle_grey))
            .transform(CircleTransform())
            .into(itemView.speakerImgView)

        itemView.setOnClickListener {
            speakerClickListener?.onClick(speaker.id)
                ?: Timber.e("Speaker Click listener on ${this::class.java.canonicalName} is null")
        }
    }
}
