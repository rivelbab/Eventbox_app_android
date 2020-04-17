package com.eventbox.app.android.sessions.track

import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class TrackConverter {

    @TypeConverter
    fun toTrack(json: String): Track? =
        jacksonObjectMapper().readerFor(Track::class.java).readValue<Track>(json)

    @TypeConverter
    fun toJson(track: Track?) = ObjectMapper().writeValueAsString(track)
}
