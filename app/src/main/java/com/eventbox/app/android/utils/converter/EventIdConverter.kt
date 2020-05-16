package com.eventbox.app.android.utils.converter

import androidx.room.TypeConverter
import com.eventbox.app.android.models.event.EventId

class EventIdConverter {

    @TypeConverter
    fun fromEventId(eventId: EventId?): String? {
        return eventId?.id
    }

    @TypeConverter
    fun toEventId(id: String?): EventId? {
        return id?.let {
            EventId(it)
        }
    }
}
