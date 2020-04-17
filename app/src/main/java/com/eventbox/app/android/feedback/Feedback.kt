package com.eventbox.app.android.feedback

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import com.eventbox.app.android.auth.UserId
import com.eventbox.app.android.event.EventId

@Type("feedback")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Feedback(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long? = null,
    val rating: String?,
    val comment: String?,
    @Relationship("event")
    var event: EventId? = null,
    @Relationship("user")
    var user: UserId? = null
)
