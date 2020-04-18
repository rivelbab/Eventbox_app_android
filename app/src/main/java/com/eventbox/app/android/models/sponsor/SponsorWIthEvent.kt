package com.eventbox.app.android.models.sponsor

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.models.sponsor.Sponsor

@Entity(
    primaryKeys = ["event_id", "sponsor_id"],
    indices = [
        Index(value = ["event_id"]),
        Index(value = ["sponsor_id"])
    ],
    foreignKeys = [
        ForeignKey(entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["event_id"]),
        ForeignKey(entity = Sponsor::class,
            parentColumns = ["id"],
            childColumns = ["sponsor_id"])
    ])
data class SponsorWithEvent(
    @ColumnInfo(name = "event_id") val eventId: Long,
    @ColumnInfo(name = "sponsor_id") val sponsorId: Long
)
