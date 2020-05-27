package com.eventbox.app.android.models.feedback

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import com.eventbox.app.android.models.user.UserId
import com.eventbox.app.android.models.event.EventId

@Type("feedback")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Feedback(
    @Id
    @PrimaryKey
    val id: String = "",
    val rating: String?,
    val comment: String?,
    var event: String? = null,
    var user: String? = null
)
