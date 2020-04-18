package com.eventbox.app.android.utils.converter

import androidx.room.TypeConverter
import com.eventbox.app.android.models.session.MicroLocation
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class MicroLocationConverter {
    @TypeConverter
    fun toMicroLoation(json: String) =
        jacksonObjectMapper().readerFor(MicroLocation::class.java).readValue<MicroLocation>(json)

    @TypeConverter
    fun toJson(microLocation: MicroLocation?) = ObjectMapper().writeValueAsString(microLocation)
}
