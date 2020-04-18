package com.eventbox.app.android.ui.event.search

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.eventbox.app.android.adapters.TextClickListener
import kotlinx.android.synthetic.main.item_location_text.view.locationText

class SearchLocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(location: String, listener: TextClickListener?) {
        itemView.locationText.text = location
        itemView.setOnClickListener {
            listener?.onTextClick(location)
        }
    }
}
