package com.eventbox.app.android.models.notification

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.github.jasminb.jsonapi.IntegerIdHandler
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.reactivex.annotations.NonNull
import java.util.*

@Type("notification")
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy::class)
@Entity
data class Notification(
    @Id
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val message: String? = null,
    @JsonProperty("receivedAt")
    val receivedAt: String? = null,
    @get:JsonProperty("isRead")
    var isRead: Boolean = false,
    val title: String? = null,
    @JsonProperty("deletedAt")
    val deletedAt: String? = null
)
