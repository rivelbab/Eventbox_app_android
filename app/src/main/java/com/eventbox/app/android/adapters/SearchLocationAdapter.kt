package com.eventbox.app.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eventbox.app.android.R
import com.eventbox.app.android.ui.event.search.SearchLocationViewHolder

class LocationsAdapter : RecyclerView.Adapter<SearchLocationViewHolder>() {
    private val locationsList = mutableListOf<String>()
    private var listener: TextClickListener? = null

    fun addAll(locations: List<String>) {
        if (locationsList.isNotEmpty()) locationsList.clear()
        locationsList.addAll(locations)
        notifyDataSetChanged()
    }

    fun setListener(listener: TextClickListener) {
        this.listener = listener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchLocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location_text, parent, false)
        return SearchLocationViewHolder(
            view
        )
    }

    override fun getItemCount(): Int = locationsList.size

    override fun onBindViewHolder(holderSearch: SearchLocationViewHolder, position: Int) {
        holderSearch.bind(locationsList[position], listener)
    }
}

interface TextClickListener {
    fun onTextClick(location: String)
}
