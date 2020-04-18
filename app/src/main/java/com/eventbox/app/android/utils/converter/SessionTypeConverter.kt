package com.eventbox.app.android.utils.converter

import androidx.room.TypeConverter
import com.eventbox.app.android.models.session.SessionType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class SessionTypeConverter {

    @TypeConverter
    fun toSessionType(json: String): SessionType? =
        jacksonObjectMapper().readerFor(SessionType::class.java).readValue<SessionType>(json)

    @TypeConverter
    fun toJson(sessionType: SessionType?) = ObjectMapper().writeValueAsString(sessionType)
}
