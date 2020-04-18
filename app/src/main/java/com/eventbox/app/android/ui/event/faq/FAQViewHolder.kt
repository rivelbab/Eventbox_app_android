package com.eventbox.app.android.ui.event.faq

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.eventbox.app.android.models.event.EventFAQ
import kotlinx.android.synthetic.main.item_faq.view.answerTv
import kotlinx.android.synthetic.main.item_faq.view.questiontv

class FAQViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(faq: EventFAQ) {
        itemView.questiontv.text = faq.question
        itemView.answerTv.text = faq.answer
    }
}
