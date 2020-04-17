package com.eventbox.app.android.search.recentsearch

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_recent_search.view.clearRecent
import kotlinx.android.synthetic.main.item_recent_search.view.recentSearchLocation
import kotlinx.android.synthetic.main.item_recent_search.view.recentSearchText

class RecentSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        searches: Pair<String, String>,
        position: Int,
        listener: RecentSearchListener?
    ) {
        itemView.recentSearchText.text = searches.first
        itemView.recentSearchLocation.text = searches.second
        itemView.setOnClickListener {
            listener?.clickSearch(searches.first, searches.second)
        }
        itemView.clearRecent.setOnClickListener {
            listener?.removeSearch(position, searches)
        }
    }
}
