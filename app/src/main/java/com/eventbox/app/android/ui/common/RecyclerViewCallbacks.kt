package com.eventbox.app.android.ui.common

import android.widget.ImageView
import com.eventbox.app.android.models.event.Event

/**
 * The callback interface for Event item clicks
 */
interface EventClickListener {
    /**
     * The function to be invoked when an event item is clicked
     *
     * @param eventID The ID of the clicked event
     * @param imageView The Image View of event object in the adapter
     */
    fun onClick(eventID: String, imageView: ImageView)
}

/**
 * The callback interface for News item clicks
 */
interface NewsClickListener {
    /**
     * The function to be invoked when an news item is clicked
     *
     * @param newsID The ID of the clicked news
     */
    fun onClick(newsID: String)
}

/**
 * The callback interface for Favorite FAB clicks
 */
interface FavoriteFabClickListener {
    /**
     * The function to be invoked when the fab is clicked
     *
     * @param event The event object for which the fab was clicked
     * @param itemPosition The position of the event object in the adapter
     */
    fun onClick(event: Event, itemPosition: Int)
}

/**
 * The callback interface for Participate FAB clicks
 */
interface InterestedFabClickListener {
    /**
     * The function to be invoked when the fab is clicked
     *
     * @param event The event object for which the fab was clicked
     * @param itemPosition The position of the event object in the adapter
     */
    fun onClick(event: Event, itemPosition: Int)
}
