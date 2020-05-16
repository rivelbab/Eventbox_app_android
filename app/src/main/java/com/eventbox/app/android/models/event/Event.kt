package com.eventbox.app.android.models.event

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

@Type("event")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Event(
    @Id
    @NonNull
    @PrimaryKey
    val id: String,
    //== basic details
    var name: String,
    var description: String? = null,
    var locationName: String? = null,
    var startsAt: String,
    var endsAt: String,
    val codeOfConduct: String? = null,
    val isComplete: Boolean = false,
    var privacy: String = "public",
    //== image url
    var originalImageUrl: String? = null,
    //== owner details
    var ownerName: String? = null,
    //== favorite and interested
    var favorite: Boolean = false,
    var favoriteEventId: String? = null,
    var eventType: String? = null
) {
    constructor(
        name: String,
        description: String,
        locationName: String,
        startsAt: String,
        endsAt: String,
        originalImageUrl: String,
        privacy: String,
        ownerName: String
    ) : this(
        "",
        name,
        description,
        locationName,
        startsAt,
        endsAt,
        null,
        false,
        privacy,
        originalImageUrl,
        ownerName
    )
}
