package com.eventbox.app.android.models.event

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import com.eventbox.app.android.models.event.EventId

@Type("event-type")
@Entity
data class EventType(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long,
    val name: String,
    val slug: String,
    @ColumnInfo(index = true)
    @Relationship("event")
    var event: EventId? = null
)
