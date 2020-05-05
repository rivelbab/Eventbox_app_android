package com.eventbox.app.android.utils.converter

import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.eventbox.app.android.models.event.EventTopic

class EventTopicConverter {
    @TypeConverter
    fun toEventTopic(json: String): EventTopic? {
        return jacksonObjectMapper().readerFor(EventTopic::class.java).readValue<EventTopic>(json)
    }

        @TypeConverter
    fun toJson(eventTopic: EventTopic?) = ObjectMapper().writeValueAsString(eventTopic)
}