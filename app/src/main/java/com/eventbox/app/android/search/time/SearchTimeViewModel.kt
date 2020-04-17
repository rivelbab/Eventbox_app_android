package com.eventbox.app.android.search.time

import androidx.lifecycle.ViewModel
import com.eventbox.app.android.data.Preference

const val SAVED_TIME = "TIME"

class SearchTimeViewModel(private val preference: Preference) : ViewModel() {

    fun saveTime(query: String) {
        preference.putString(SAVED_TIME, query)
    }
}
