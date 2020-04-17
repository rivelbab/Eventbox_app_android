package com.eventbox.app.android.data

import android.content.Context
import android.net.ConnectivityManager
import com.eventbox.app.android.EventboxEntryPoint

class Network {

    private val context by lazy {
        EventboxEntryPoint.appContext
    }

    private val connectivityManager by lazy {
        context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    }

    fun isNetworkConnected(): Boolean {
        return connectivityManager?.activeNetworkInfo != null
    }
}
