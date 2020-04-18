package com.eventbox.app.android.utils.converter

import androidx.room.TypeConverter
import com.eventbox.app.android.models.event.EventSubTopic
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class EventSubTopicConverter {

    @TypeConverter
    fun toEventSubTopic(json: String): EventSubTopic? {
        return jacksonObjectMapper().readerFor(EventSubTopic::class.java).readValue<EventSubTopic>(json)
    }

    @TypeConverter
    fun toJson(eventSubTopic: EventSubTopic?) = ObjectMapper().writeValueAsString(eventSubTopic)
}
