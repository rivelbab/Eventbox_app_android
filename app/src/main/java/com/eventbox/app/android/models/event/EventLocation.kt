package com.eventbox.app.android.models.event

import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("event-location")
data class EventLocation(
    @Id
    val id: String,
    val name: String,
    val slug: String
)
