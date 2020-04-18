package com.eventbox.app.android.ui.event.search

import androidx.lifecycle.ViewModel
import com.eventbox.app.android.config.Preference

const val SAVED_TIME = "TIME"

class SearchTimeViewModel(private val preference: Preference) : ViewModel() {

    fun saveTime(query: String) {
        preference.putString(SAVED_TIME, query)
    }
}
