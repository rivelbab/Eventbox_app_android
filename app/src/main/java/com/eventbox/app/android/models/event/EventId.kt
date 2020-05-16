package com.eventbox.app.android.models.event

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("event")
data class EventId(
    @Id
    val id: String
)
