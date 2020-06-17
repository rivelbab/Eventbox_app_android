package com.eventbox.app.android.models.event

import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("faq")
data class EventFAQ(
    @Id
    val id: String = "",
    val question: String,
    val answer: String
)
