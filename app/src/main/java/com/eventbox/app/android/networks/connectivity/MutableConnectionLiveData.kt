package com.eventbox.app.android.networks.connectivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.MutableLiveData
import com.eventbox.app.android.EventboxEntryPoint
import com.eventbox.app.android.utils.Utils

class MutableConnectionLiveData : MutableLiveData<Boolean>() {
    private val context by lazy {
        EventboxEntryPoint.appContext
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            postValue(Utils.isNetworkConnected(context))
        }
    }

    override fun onActive() {
        super.onActive()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context?.registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onInactive() {
        super.onInactive()
        context?.unregisterReceiver(broadcastReceiver)
    }
}
