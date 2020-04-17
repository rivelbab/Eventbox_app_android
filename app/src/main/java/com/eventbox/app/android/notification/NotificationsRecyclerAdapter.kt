package com.eventbox.app.android.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eventbox.app.android.R
import com.eventbox.app.android.notification.NotificationsViewHolder

class NotificationsRecyclerAdapter : RecyclerView.Adapter<NotificationsViewHolder>() {

    private val notificationList = ArrayList<Notification>()

    fun addAll(list: List<Notification>) {
        notificationList.clear()
        notificationList.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_notification, parent, false)
        return NotificationsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int) {
        holder.bind(notificationList[position])
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}
