package com.eventbox.app.android.models.speakers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import com.eventbox.app.android.models.event.EventId
import com.eventbox.app.android.models.session.Track

@Type("session")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Proposal(
    @PrimaryKey
    @Id
    val id: Long? = null,
    val title: String? = null,
    val language: String? = null,
    val shortAbstract: String? = null,
    val comments: String? = null,
    val startsAt: String? = null,
    val endsAt: String? = null,
    val subTitle: String? = null,
    val longAbstract: String? = null,
    @ColumnInfo(index = true)
    @Relationship("track", resolve = true)
    val track: Track? = null,
    @ColumnInfo(index = true)
    @Relationship("event", resolve = true)
    val event: EventId? = null,
    @ColumnInfo(index = true)
    @Relationship("speakers", resolve = true)
    val speakers: List<SpeakerId> = emptyList()
)
