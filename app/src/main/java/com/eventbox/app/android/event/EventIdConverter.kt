package com.eventbox.app.android.event

import androidx.room.TypeConverter

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
