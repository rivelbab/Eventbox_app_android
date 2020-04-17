package com.eventbox.app.android.event.topic

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import com.eventbox.app.android.event.Event
import com.eventbox.app.android.event.EventId

@Type("event-topic")
@Entity(foreignKeys = [(ForeignKey(entity = Event::class, parentColumns = ["id"],
    childColumns = ["event"], onDelete = ForeignKey.CASCADE))])
data class EventTopic(
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long,
    val name: String,
    val slug: String,
    @ColumnInfo(index = true)
    @Relationship("event")
    var event: EventId? = null
)
