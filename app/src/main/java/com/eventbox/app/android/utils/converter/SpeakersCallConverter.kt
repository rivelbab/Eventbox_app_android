package com.eventbox.app.android.utils.converter

import androidx.room.TypeConverter
import com.eventbox.app.android.models.speakers.SpeakersCall
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class SpeakersCallConverter {
    @TypeConverter
    fun toSpeakersCall(json: String): SpeakersCall? {
        return jacksonObjectMapper().readerFor(SpeakersCall::class.java).readValue<SpeakersCall>(json)
    }

    @TypeConverter
    fun toJson(speakersCall: SpeakersCall?) = ObjectMapper().writeValueAsString(speakersCall)
}
