package com.eventbox.app.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.eventbox.app.android.R
import com.eventbox.app.android.ui.event.search.SearchPlaceSuggestionViewHolder

/**
 * The RecyclerView adapter class for displaying lists of Place Suggestions.
 */

class SearchPlaceSuggestionsAdapter :
    ListAdapter<CarmenFeature,
            SearchPlaceSuggestionViewHolder>(PlaceDiffCallback()) {

    var onSuggestionClick: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPlaceSuggestionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place_suggestion, parent, false)
        return SearchPlaceSuggestionViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(holderSearch: SearchPlaceSuggestionViewHolder, position: Int) {
        holderSearch.apply {
            bind(getItem(position))
            onSuggestionClick = this@SearchPlaceSuggestionsAdapter.onSuggestionClick
        }
    }

    class PlaceDiffCallback : DiffUtil.ItemCallback<CarmenFeature>() {
        override fun areItemsTheSame(oldItem: CarmenFeature, newItem: CarmenFeature): Boolean {
            return oldItem.placeName() == newItem.placeName()
        }

        override fun areContentsTheSame(oldItem: CarmenFeature, newItem: CarmenFeature): Boolean {
            return oldItem.equals(newItem)
        }
    }
}
