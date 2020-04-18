package com.eventbox.app.android.utils.converter

import androidx.room.TypeConverter
import com.eventbox.app.android.models.event.EventId

class EventIdConverter {

    @TypeConverter
    fun fromEventId(eventId: EventId?): Long? {
        return eventId?.id
    }

    @TypeConverter
    fun toEventId(id: Long?): EventId? {
        return id?.let {
            EventId(it)
        }
    }
}
