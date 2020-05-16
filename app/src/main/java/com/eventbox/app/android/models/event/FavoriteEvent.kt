package com.eventbox.app.android.models.event

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import com.eventbox.app.android.models.event.EventId

@Type("user-favourite-event")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
data class FavoriteEvent(
    @Id
    @PrimaryKey
    @NonNull
    val id: String,
    @ColumnInfo(index = true)
    @Relationship("event", resolve = true)
    val event: EventId? = null
)
