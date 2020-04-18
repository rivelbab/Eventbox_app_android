package com.eventbox.app.android.ui.event.search

import androidx.lifecycle.ViewModel
import java.lang.StringBuilder
import com.eventbox.app.android.R
import com.eventbox.app.android.config.Preference
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.fragments.event.search.RECENT_SEARCHES

class SearchViewModel(
    private val preference: Preference,
    private val resource: Resource
) : ViewModel() {

    var savedLocation: String? = null
    var savedType: String? = null
    var savedTime: String? = null
    var isQuerying = false
    private val recentSearches = mutableListOf<Pair<String, String>>()

    fun getRecentSearches(): List<Pair<String, String>> {
        val searchesStrings = preference.getString(RECENT_SEARCHES, "")
        if (recentSearches.isNotEmpty()) recentSearches.clear()
        if (!searchesStrings.isNullOrEmpty()) {
            val searches = searchesStrings.split(",")
            searches.forEach {
                val searchAndLocation = it.split("/")
                recentSearches.add(Pair(searchAndLocation[0], searchAndLocation[1]))
            }
        }
        return recentSearches
    }

    fun saveRecentSearch(query: String, location: String, position: Int = 0) {
        if (query.isEmpty() || location.isEmpty() || location == resource.getString(R.string.enter_location)) return
        recentSearches.add(position, Pair(query, location))
        saveRecentSearchToPreference()
    }

    fun removeRecentSearch(position: Int) {
        recentSearches.removeAt(position)
        saveRecentSearchToPreference()
    }

    private fun saveRecentSearchToPreference() {
        val builder = StringBuilder()
        for ((index, pair) in recentSearches.withIndex()) {
            builder.append(pair.first).append("/").append(pair.second)
            if (index != recentSearches.size - 1) builder.append(",")
        }
        preference.putString(RECENT_SEARCHES, builder.toString())
    }

    fun loadSavedLocation() {
        savedLocation = preference.getString(SAVED_LOCATION) ?: resource.getString(R.string.enter_location)
    }
    fun loadSavedType() {
        savedType = preference.getString(SAVED_TYPE)
    }
    fun loadSavedTime() {
        savedTime = preference.getString(SAVED_TIME)
    }
}
