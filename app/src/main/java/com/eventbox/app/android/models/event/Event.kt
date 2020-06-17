package com.eventbox.app.android.models.event

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.LongIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import java.util.*

@Type("event")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Event(
    @Id
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    //== basic details
    var name: String,
    var description: String? = null,
    @JsonProperty("locationName")
    var locationName: String? = null,
    @JsonProperty("startsAt")
    var startsAt: String,
    @JsonProperty("endsAt")
    var endsAt: String,
    @JsonProperty("codeOfConduct")
    val codeOfConduct: String? = null,
    @JsonProperty("isComplete")
    val isComplete: Boolean = false,
    var privacy: String = "public",
    //== image url
    @JsonProperty("originalImageUrl")
    var originalImageUrl: String? = null,
    //== owner details
    @JsonProperty("ownerName")
    var ownerName: String? = null,
    //== favorite and interested
    var category: String? = null,
    @JsonIgnore
    var favorite: Boolean = false,
    @JsonIgnore
    var favoriteEventId: String? = null,
    @JsonIgnore
    var eventType: String? = null
)
