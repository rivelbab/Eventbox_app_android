package com.eventbox.app.android.models.event

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
    @Id(LongIdHandler::class)
    @PrimaryKey
    val id: Long,
    //== basic details
    val name: String,
    val identifier: String,
    val description: String? = null,
    val locationName: String? = null,
    val startsAt: String,
    val endsAt: String,
    val codeOfConduct: String? = null,
    val createdAt: String? = null,
    val deletedAt: String? = null,
    val isComplete: Boolean = false,
    val privacy: String = "public",
    //== image url
    val thumbnailImageUrl: String? = null,
    val originalImageUrl: String? = null,
    val largeImageUrl: String? = null,
    //== owner details
    val ownerDescription: String? = null,
    val ownerName: String? = null,
    val hasOwnerInfo: Boolean = false,
    //== geolocation details
    val timezone: String = "UTC",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isMapShown: Boolean = false,
    //== favorite and interested
    var favorite: Boolean = false,
    var favoriteEventId: Long? = null,
    var interested: Boolean = false,
    var interestedEventId: Long? = null,
    //== payment details
    val paymentCurrency: String? = null,
    val paymentCountry: String? = null,
    val paypalEmail: String? = null,
    val ticketUrl: String? = null,
    val refundPolicy: String? = null,
    val canPayByStripe: Boolean = false,
    val canPayByBank: Boolean = false,
    val canPayByPaypal: Boolean = false,
    val canPayOnsite: Boolean = false,
    val isTicketingEnabled: Boolean = false,
    val isTaxEnabled: Boolean = false,

    @ColumnInfo(index = true)
    @Relationship("event-topic", resolve = true)
    val eventTopic: EventTopic? = null
)
